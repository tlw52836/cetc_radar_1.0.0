package codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.UUID;

public class SaveHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {

        //String req = packet.content().toString(CharsetUtil.UTF_8);//上面说了，通过content()来获取消息内容
        ByteBuf content = packet.content();
        byte[] buf = new byte[content.readableBytes()];
        content.readBytes(buf);


        UUID uuid = UUID.randomUUID();
        String path = "E:\\json\\" + uuid + ".txt";
        OutputStream os = new FileOutputStream(new File(path));
        os.write(buf, 0, buf.length);

        os.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        cause.printStackTrace();
    }

}
