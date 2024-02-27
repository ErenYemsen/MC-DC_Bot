package me.ErenY.myaudiomanager;

import net.dv8tion.jda.api.audio.AudioSendHandler;

import java.nio.ByteBuffer;

public class MySendHandler implements AudioSendHandler {
    @Override
    public boolean canProvide() {
        return false;
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        return null;
    }

    @Override
    public boolean isOpus() {
        return AudioSendHandler.super.isOpus();
    }
}
