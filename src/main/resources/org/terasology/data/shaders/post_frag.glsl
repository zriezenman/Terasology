/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

uniform sampler2D texScene;
uniform sampler2D texBloom;
uniform sampler2D texBlur;
uniform sampler2D texVignette;
uniform sampler2D texDepth;

uniform bool swimming;

uniform float fogIntensity = 0.1;
uniform float fogLinearIntensity = 0.1;
uniform float viewingDistance;

uniform mat4 invViewProjMatrix;
uniform mat4 prevViewProjMatrix;

#define Z_NEAR 0.1
#define BLUR_START 0.6
#define BLUR_LENGTH 0.05
#define LAYERED_FOG

#define MOTION_BLUR
#define MOTION_BLUR_SAMPLES 8

float linDepth() {
    float z = texture2D(texDepth, gl_TexCoord[0].xy).x;
    return (2.0 * Z_NEAR) / (viewingDistance + Z_NEAR - z * (viewingDistance - Z_NEAR));
}

void main() {
    /* BLUR */
    vec4 colorBlur = texture2D(texBlur, gl_TexCoord[0].xy);

    float depth = linDepth();
    float blur = 0.0;

    if (depth > BLUR_START && !swimming)
       blur = clamp((depth - BLUR_START) / BLUR_LENGTH, 0.0, 1.0);
    else if (swimming)
       blur = 1.0;

    /* COLOR AND BLOOM */
    vec4 color = texture2D(texScene, gl_TexCoord[0].xy);
    vec4 colorBloom = texture2D(texBloom, gl_TexCoord[0].xy);

    color = clamp(color + colorBloom, 0.0, 1.0);
    colorBlur = clamp(colorBlur + colorBloom, 0.0, 1.0);

#ifdef MOTION_BLUR
    float zOverW = texture2D(texDepth, gl_TexCoord[0].xy).x;
    vec4 screenSpacePos = vec4(gl_TexCoord[0].x, gl_TexCoord[0].y, zOverW, 1.0) * 2.0 - 1.0;
    vec4 worldSpacePos = invViewProjMatrix * screenSpacePos;
    vec4 normWorldSpacePos = worldSpacePos / worldSpacePos.w;
    vec4 prevScreenSpacePos = prevViewProjMatrix * normWorldSpacePos;
    prevScreenSpacePos /= prevScreenSpacePos.w;

    vec2 velocity = (screenSpacePos.xy - prevScreenSpacePos.xy) / 12.0;

    if (length(velocity) > 0.005) {
        velocity = clamp(velocity, vec2(-0.05), vec2(0.05));

        vec2 blurTexCoord = gl_TexCoord[0].xy;
        blurTexCoord += velocity;
        for(int i = 1; i < MOTION_BLUR_SAMPLES; ++i, blurTexCoord += velocity)
        {
          vec4 currentColor = texture2D(texScene, blurTexCoord);
          vec4 currentColorBlur = texture2D(texBlur, blurTexCoord);

          color += currentColor;
          colorBlur += currentColorBlur;
        }

        color /= MOTION_BLUR_SAMPLES;
        colorBlur /= MOTION_BLUR_SAMPLES;
    }
#endif

    /* FINAL MIX */
    vec4 finalColor = mix(color, colorBlur, blur);

    if (fogIntensity > 0.0 || fogLinearIntensity > 0.0) {
        float fogDensity = depth * fogIntensity;
        float fog = clamp((1.0 - 1.0 / pow(2.71828, fogDensity * fogDensity)) + depth * fogLinearIntensity, 0.0, 1.0);
        finalColor = mix(finalColor, vec4(1.0), fog);
    }

    /* VIGNETTE */
    float vig = texture2D(texVignette, gl_TexCoord[0].xy).x;

    if (!swimming)
        finalColor.rgb *= vig;
    else
        finalColor.rgb *= vig / 4.0;

    //gl_FragColor = vec4(vec3(length(velocity)), 1.0);
    gl_FragColor = finalColor;
}
