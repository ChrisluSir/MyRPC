package com.chris.rpc.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * RPC解码器 --> 反序列化
 * RpcDecoder
 * author: Chris
 * time：2018.03.05 11:48
 */
public class RpcDecoder extends ByteToMessageDecoder {

    private Class<?> genericClass;

    //构造函数传入反序列化的class
    public RpcDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    protected void decode(ChannelHandlerContext ctx, ByteBuf inData, List<Object> outList) throws Exception {
        if (inData.readableBytes() < 4) {
            return;
        }
        inData.markReaderIndex();
        int dataLen = inData.readInt();
        if (dataLen < 0) {
            ctx.close();
        }
        if (inData.readableBytes() < dataLen) {
            inData.resetReaderIndex();
        }
        //将ByteBuf转换为byte[]
        byte[] data = new byte[dataLen];
        inData.readBytes(data);
        //将data转换为Object
        Object object = SerializationUtil.deserialize(data, genericClass);
        outList.add(object);
    }
}
