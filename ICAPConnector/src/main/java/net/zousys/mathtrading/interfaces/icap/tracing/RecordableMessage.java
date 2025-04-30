package net.zousys.mathtrading.interfaces.icap.tracing;

import com.icap.iConnect.srcMsgs.enums.EICMsgType;
import com.icap.iConnect.srcMsgs.iCMsg.*;
import com.icap.iConnect.srcMsgs.iCUtils.ICMessageBuffer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.zousys.mathtrading.interfaces.icap.ICAPMessage;
import net.zousys.mathtrading.interfaces.util.FileReader;

import java.io.IOException;
import java.nio.ByteBuffer;
@AllArgsConstructor
public class  RecordableMessage <T extends ICMsg> {
    @Getter
    protected T icMsg;
    @Getter
    protected long time;

    /**
     *
     * @param name
     * @return
     * @throws IOException
     */
    public static RecordableMessage parse(String name) throws IOException {
        return parse(FileReader.readFileToBytes(name));
    }
    /**
     *
     * @param data
     * @return
     */
    public static RecordableMessage parse(byte[] data) {
        ByteBuffer bb = ByteBuffer.wrap(data);
        byte type = bb.get();
        ICMsg icMsgr = new ICMsg();
        icMsgr.setMsgType(EICMsgType.getName((int)type));
        bb.position(1);
        ByteBuffer sub = bb.slice();
        ICMessageBuffer mb = new ICMessageBuffer(sub);
        mb.flip();
        mb.limit(mb.capacity());
        RecordableMessage rmessage = new RecordableMessage(icMsgr, System.currentTimeMillis());
        rmessage.unmashall(mb, true);
        return rmessage;
    }

    /**
     *
     * @return
     */
    public byte[] serialize() {
        ByteBuffer mbb = getByteBuffer();
        ByteBuffer bb = ByteBuffer.allocate(mbb.capacity()+1);
        bb.put((byte)icMsg.getMsgType().getValue());
        bb.put(mbb);
        return bb.array();
    }

    /**
     *
     * @return
     */
    public final ByteBuffer getByteBuffer() {
        ICMessageBuffer mb = new ICMessageBuffer();
        getICMessage(false).pack(mb);
        return mb.getBuffer();
    }

    /**
     *
     * @param buffer
     * @param init
     */
    protected void unmashall(ICMessageBuffer buffer, boolean init) {
        icMsg = getICMessage(init);
        icMsg.unpack(buffer);
    }
    /**
     *
     * @return
     * @param <T>
     */
    @SuppressWarnings("unchecked")
    public <T extends ICMsg> T getICMessage(boolean init) {
        EICMsgType type = icMsg.getMsgType();
        switch (type) {
            case EICMsgType.eMsgPositiveLogin: {
                return init? (T) new ICMsgPositiveLogin():(T) ((ICMsgPositiveLogin) icMsg);
            }
            case EICMsgType.eMsgClearBook: {
                return init? (T) new ICMsgClearBookUpdate():(T) ((ICMsgClearBookUpdate)icMsg);
            }
            case EICMsgType.eMsgMessageLogUpdate: {
                return init? (T) new ICMsgLogUpdate():(T) ((ICMsgLogUpdate)icMsg);
            }
            case EICMsgType.eMsgPositive: {
                return init? (T) new ICMsgPositive():(T) ((ICMsgPositive) icMsg);
            }
            case EICMsgType.eMsgHeartbeat: {
                return init? (T) new ICMsgHeartbeat():(T) ((ICMsgHeartbeat) icMsg);
            }
            default: {
                return init? (T) new ICMsg():(T) icMsg;
            }
        }
    }

        public static final byte[] fromByteBuffer(ByteBuffer bb, int start, int end) {
            bb.rewind();
            bb.limit(bb.capacity());
            if (start < 0) {
                start = 0;
            }
            if (end > bb.capacity()|| end < 0) {
                end = bb.capacity();
            }
            byte[] extracted = new byte[end - start];
            bb.position(start); // Set position to startIndex
            bb.get(extracted, start, end - start); // Extract bytes
            return extracted;
        }
        public static void main(String[] args) throws IOException {
            ICMsg icMsgr = new ICMsg();
            ICMessageBuffer mb = new ICMessageBuffer();
            icMsgr.pack(mb);
            byte[] data = fromByteBuffer(mb.getBuffer(), -1, -1);

            ICMsg icMsgr2 = new ICMsg();
            ICMessageBuffer mb2 = new ICMessageBuffer();
            mb2.put(data);
//            mb2.rewind();
            icMsgr2.unpack(mb2);

            ICAPMessage irm = new ICAPMessage(icMsgr2);
            byte[] xx = irm.serialize();
            int a =1;
        }


}
