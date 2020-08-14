package com.bgsoftware.wildloaders.npc;

import io.netty.channel.AbstractChannel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.EventLoop;

import java.net.SocketAddress;

public final class DummyChannel extends AbstractChannel {

    public DummyChannel(){
        super(null);
    }

    @Override
    protected AbstractUnsafe newUnsafe() {
        return null;
    }

    @Override
    protected boolean isCompatible(EventLoop eventLoop) {
        return false;
    }

    @Override
    protected SocketAddress localAddress0() {
        return null;
    }

    @Override
    protected SocketAddress remoteAddress0() {
        return null;
    }

    @Override
    protected void doBind(SocketAddress socketAddress){

    }

    @Override
    protected void doDisconnect(){

    }

    @Override
    protected void doClose(){

    }

    @Override
    protected void doBeginRead(){

    }

    @Override
    protected void doWrite(ChannelOutboundBuffer channelOutboundBuffer){

    }

    @Override
    public ChannelConfig config() {
        return null;
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public ChannelMetadata metadata() {
        return null;
    }
}
