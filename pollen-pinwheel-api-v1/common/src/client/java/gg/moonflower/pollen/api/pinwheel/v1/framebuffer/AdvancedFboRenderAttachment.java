package gg.moonflower.pollen.api.pinwheel.v1.framebuffer;

import com.mojang.blaze3d.systems.RenderSystem;
import gg.moonflower.pollen.impl.pinwheel.framebuffer.AdvancedFboImpl;
import org.apache.commons.lang3.Validate;

import static org.lwjgl.opengl.GL30.*;

/**
 * An attachment for an {@link AdvancedFboImpl} that represents a depth render buffer.
 *
 * @author Ocelot
 * @since 1.0.0
 */
public class AdvancedFboRenderAttachment implements AdvancedFboAttachment {

    public static final int MAX_SAMPLES = glGetInteger(GL_MAX_SAMPLES);

    private int id;
    private final int attachmentType;
    private final int attachmentFormat;
    private final int width;
    private final int height;
    private final int samples;

    public AdvancedFboRenderAttachment(int attachmentType, int attachmentFormat, int width, int height, int samples) {
        this.attachmentType = attachmentType;
        this.attachmentFormat = attachmentFormat;
        this.width = width;
        this.height = height;
        Validate.inclusiveBetween(1, MAX_SAMPLES, samples);
        this.samples = samples;
    }

    @Override
    public void create() {
        this.bindAttachment();
        if (this.samples == 1) {
            glRenderbufferStorage(GL_RENDERBUFFER, this.attachmentFormat, this.width, this.height);
        } else {
            glRenderbufferStorageMultisample(GL_RENDERBUFFER, this.samples, this.attachmentFormat, this.width, this.height);
        }
        this.unbindAttachment();
    }

    @Override
    public void attach(int target, int attachment) {
        Validate.isTrue(this.attachmentType != GL_DEPTH_ATTACHMENT || attachment == 0, "Only one depth buffer attachment is supported.");
        glFramebufferRenderbuffer(target, this.attachmentType, GL_RENDERBUFFER, this.getId());
    }

    @Override
    public void bindAttachment() {
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> glBindRenderbuffer(GL_RENDERBUFFER, this.getId()));
        } else {
            glBindRenderbuffer(GL_RENDERBUFFER, this.getId());
        }
    }

    @Override
    public void unbindAttachment() {
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> glBindRenderbuffer(GL_RENDERBUFFER, 0));
        } else {
            glBindRenderbuffer(GL_RENDERBUFFER, 0);
        }
    }

    public int getId() {
        RenderSystem.assertOnRenderThreadOrInit();
        if (this.id == 0) {
            this.id = glGenRenderbuffers();
        }

        return this.id;
    }

    @Override
    public int getAttachmentType() {
        return attachmentType;
    }

    @Override
    public int getFormat() {
        return attachmentFormat;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getLevels() {
        return samples;
    }

    @Override
    public boolean canSample() {
        return false;
    }

    @Override
    public AdvancedFboAttachment createCopy() {
        return new AdvancedFboRenderAttachment(this.attachmentType, this.attachmentFormat, this.width, this.height, this.samples);
    }

    @Override
    public void free() {
        if (this.id != 0) {
            glDeleteRenderbuffers(this.id);
        }
        this.id = 0;
    }
}
