package net.zousys.mathtrading.interfaces.icap.tracing;

import com.icap.iConnect.srcMsgs.enums.EICMsgType;
import com.icap.iConnect.srcMsgs.iCMsg.*;
import com.icap.iConnect.srcMsgs.iCUtils.ICMessageBuffer;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.ByteBuffer;
@AllArgsConstructor
public class  RecordableMessage {
    @Getter
    protected ICMsg icMsg;

    /**
     *
     * @param data
     * @return
     */
    public static <T extends ICMsg> T parse(byte[] data) {
        ByteBuffer bb = ByteBuffer.wrap(data);
        byte type = bb.get();
        ICMsg icMsgr = new ICMsg();
        icMsgr.setMsgType(EICMsgType.getName((int)type));
        ByteBuffer sub = bb.slice();
        ICMessageBuffer mb = new ICMessageBuffer(sub);
        return (T) new RecordableMessage(icMsgr).getICMessage(true);
    }

    /**
     *
     * @return
     */
    public byte[] serialize() {
        ByteBuffer bb = ByteBuffer.allocate(201);
        bb.put((byte)icMsg.getMsgType().getValue());
        bb.put(getByteBuffer());
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



    public static void main(String[] args) {
        ICMsgLogUpdate mlu = new ICMsgLogUpdate();
        ICMsgClearBookUpdate mcbu = new ICMsgClearBookUpdate();
        byte[] b1 = new RecordableMessage(mlu).serialize();
        byte[] b2 = new RecordableMessage(mcbu).serialize();
        byte[] b3 = new RecordableMessage(new ICMsg()).serialize();
        int s = 0;
    }

}
