#ifdef GL_ES
precision highp float;
#endif
uniform sampler2D u_texture;
uniform sampler2D u_blink;
uniform float u_time;
varying vec2 v_uv;

const vec2 size = vec2(320.0, 180.0);
const vec4 ink = vec4(25.0, 23.0, 28.0, 255.0) / 255.0;

vec2 texel(vec2 p) {
    return (clamp(floor(p + 0.5), vec2(0.0), size - 1.0) + 0.5) / size;
}

void main() {
    vec2 pixel = floor(v_uv * size);
    float t = u_time;
    // Anchor the foot of the silhouette. Sample at whole pixels even on the curved edges.
    float sway = 1.0 - smoothstep(25.0, 120.0, pixel.y);
    vec2 p = pixel - sway * vec2(2.4 * sin(1.6*t + pixel.y*0.016),
                                 1.3 * sin(2.1*t + pixel.x*0.019));
    vec2 q = p;
    bool eye = p.x > 31.0 && p.x < 126.0 && p.y > 10.0 && p.y < 55.0;
    bool mouth = p.x > 34.0 && p.x < 136.0 && p.y >= 55.0 && p.y < 117.0;
    if (eye) {
        q -= vec2(1.3*sin(2.8*t), sin(2.2*t + p.x*0.06));
        float blink = mod(t, 4.5);
        if (blink >= 4.14 && blink < 4.27) {
            gl_FragColor = texture2D(u_blink, texel(q));
            return;
        }
        // Fold the open eye toward the lid before and after the closed pose.
        float opening = 1.0 - 0.78 * smoothstep(4.0, 4.14, blink)
                             + 0.78 * smoothstep(4.27, 4.43, blink);
        q.y = 45.0 + (q.y - 45.0) / opening;
        if (q.y < 13.0 || q.y > 53.0) {
            gl_FragColor = ink;
            return;
        }
        // Redraw the supplied pupil's ink strokes in short, uneven steps.
        float pupil = 1.0 - smoothstep(0.78, 1.18, length((q - vec2(78.0, 35.5)) / vec2(22.0, 16.5)));
        float frame = floor(t * 9.0);
        q += pupil * vec2(1.8*sin(frame*2.4 + q.y*0.62), 1.5*sin(frame*1.8 + q.x*0.48));
    } else if (mouth) {
        q.x -= 1.4*sin(2.4*t + p.y*0.07);
        q.y = 84.0 + (p.y - 84.0) / (1.0 + 0.055*sin(2.7*t)) - sin(2.4*t + p.x*0.08);
    }
    vec4 moving = texture2D(u_texture, texel(q));
    vec4 original = texture2D(u_texture, texel(pixel));
    // Both reds belong to the halftone sky. Hold its dots steady as the silhouette sways.
    gl_FragColor = !eye && !mouth && moving.r > 0.2 && original.r > 0.2 ? original : moving;
}
