package com.chris.rpc.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * RPC编码器
 * RpcEncoder
 * author: Chris
 * time：2018.03.05 11:54
 */
public class RpcEncoder extends MessageToByteEncoder {

    private Class<?> genericClass;

    //构造函数传入序列化的class
    public RpcEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    protected void encode(ChannelHandlerContext ctx, Object inO, ByteBuf outData) throws Exception {
        //序列化
        if (genericClass.isInstance(inO)) {
            byte[] data = SerializationUtil.serialize(inO);
            outData.writeInt(data.length);
            outData.writeBytes(data);
        }
    }
}
