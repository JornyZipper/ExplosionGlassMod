package bigworld.client;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;

public class GLShaderProgram {
    private int program = 0;

    public GLShaderProgram() {}

    public boolean createFromResources(String vertPath, String fragPath) {
        try {
            String vert = readResource(vertPath);
            String frag = readResource(fragPath);
            int vs = compile(GL20.GL_VERTEX_SHADER, vert);
            int fs = compile(GL20.GL_FRAGMENT_SHADER, frag);
            program = GL20.glCreateProgram();
            GL20.glAttachShader(program, vs);
            GL20.glAttachShader(program, fs);
            GL20.glLinkProgram(program);
            int linked = GL20.glGetProgrami(program, GL20.GL_LINK_STATUS);
            if (linked == GL11.GL_FALSE) {
                String log = GL20.glGetProgramInfoLog(program, 1024);
                System.err.println("GLShaderProgram link error: " + log);
                return false;
            }
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    private int compile(int type, String source) throws Exception {
        int sh = GL20.glCreateShader(type);
        GL20.glShaderSource(sh, source);
        GL20.glCompileShader(sh);
        int ok = GL20.glGetShaderi(sh, GL20.GL_COMPILE_STATUS);
        if (ok == GL11.GL_FALSE) {
            String log = GL20.glGetShaderInfoLog(sh, 1024);
            throw new Exception("Shader compile failed: " + log);
        }
        return sh;
    }

    private String readResource(String path) throws Exception {
        InputStream in = getClass().getResourceAsStream(path);
        if (in == null) throw new Exception("Resource not found: " + path);
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append('\n');
        }
        br.close();
        return sb.toString();
    }

    public void use() { if (program != 0) GL20.glUseProgram(program); }
    public void stop() { GL20.glUseProgram(0); }

    public void setUniformi(String name, int value) {
        int loc = GL20.glGetUniformLocation(program, name);
        if (loc >= 0) GL20.glUniform1i(loc, value);
    }

    public void setUniformMatrix4f(String name, FloatBuffer buf) {
        int loc = GL20.glGetUniformLocation(program, name);
        if (loc >= 0) GL20.glUniformMatrix4(loc, false, buf);
    }
}
