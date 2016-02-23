precision highp float;
uniform mat4 uMVPMatrix;

void main() {
    // Transform given position to coordinate space
    vec2 p = (uMVPMatrix * vec4(gl_PointCoord,0,1)).xy;
    vec2 c = p;
    // Set default color to black in HSV
    vec3 color = vec3(0.0, 0.0, 0.0);
    // Use 200 as an arbitrary limit. The higher the number, the slower and more detailed it will be
    for(int i = 0; i < 200; i++) {
        // Perform z = z^2 + c using p, which represents the real and imaginary parts of z
        p = vec2(p.x*p.x - p.y*p.y, 2.0*p.x*p.y) + c;
        if (dot(p, p) > 4.0) {
            // colorRegulator continuously increases smoothly by 1 for every additional step it takes to break
            float colorRegulator = float(i - 1) - log(log(length(p)))/log(2.0);
            // Set color to a cycling color scheme using the smooth number
            color = vec3(0.95 + .012*colorRegulator, 1.0, .2 + .4*(1.0 + sin(.3*colorRegulator)));
            break;
        }
    }
    //Convert HSV to RGB. Algorithm from https://gist.github.com/patriciogonzalezvivo/114c1653de9e3da6e1e3
    vec4 K = vec4(1.0, 2.0/3.0, 1.0/3.0, 3.0);
    vec3 m = abs(fract(color.xxx + K.xyz)*6.0 - K.www);
    gl_FragColor.rgb = color.z * mix(K.xxx, clamp(m - K.xxx, 0.0, 1.0), color.y);
    gl_FragColor.a = 1.0;
}
