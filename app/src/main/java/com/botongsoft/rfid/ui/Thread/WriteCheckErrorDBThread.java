package com.botongsoft.rfid.ui.Thread;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.botongsoft.rfid.bean.classity.CheckError;
import com.botongsoft.rfid.common.db.DBDataUtils;

import java.util.ArrayList;
import java.util.List;

import static com.botongsoft.rfid.common.constants.Constant.BackThread_PUT_CHECKERROR_SUCCESS_PB;

/**
 * Created by pc on 2017/7/19.
 */

public class WriteCheckErrorDBThread extends Thread {
    private List<CheckError> objList = null;
    private List<CheckError> saveList = new ArrayList<CheckError>();
    private List<CheckError> newList = new ArrayList<CheckError>();
    private CheckError mCheckErrorOld;
    private Handler mhandler;
    private Message uiMsg;

    public WriteCheckErrorDBThread(Handler mhandler, Message uiMsg) {
        this.mhandler = mhandler;
        this.uiMsg = uiMsg;
    }

    public void setList(List list) {
        this.objList = list;
    }

    @Override
    public void run() {
        int size =objList.size();
        for (int i = 0; i < size; i++) {
            CheckError mCheckError = objList.get(i);
            uiMsg = mhandler.obtainMessage();
            Bundle b = new Bundle();
            b.putInt("checkerror",i+1);
            uiMsg.setData(b);
            uiMsg.what = BackThread_PUT_CHECKERROR_SUCCESS_PB;
            mhandler.sendMessage(uiMsg);
            mCheckErrorOld = (CheckError) DBDataUtils.getInfo(CheckError.class, "pdid", String.valueOf(mCheckError.getPdid()),
                    "zy", String.valueOf(mCheckError.getZy()),
                    "kfid", String.valueOf(mCheckError.getKfid()),
                    "mjgid", String.valueOf(mCheckError.getMjgid()),
                    "mjjid", String.valueOf(mCheckError.getMjjid()));
            if (mCheckErrorOld != null) {
                mCheckErrorOld.setAnchor(mCheckError.getAnchor());
                mCheckErrorOld.setId(mCheckError.getId());
                mCheckErrorOld.setPdid(mCheckError.getPdid());
                mCheckErrorOld.setStatus(9);
                saveList.add(mCheckErrorOld);
            } else {
                mCheckError.setStatus(9);
                newList.add(mCheckError);
            }
        }
//        for (CheckError mCheckError : objList) {
//            mCheckErrorOld = (CheckError) DBDataUtils.getInfo(CheckError.class, "pdid", String.valueOf(mCheckError.getPdid()),
//                    "zy", String.valueOf(mCheckError.getZy()),
//                    "kfid", String.valueOf(mCheckError.getKfid()),
//                    "mjgid", String.valueOf(mCheckError.getMjgid()),
//                    "mjjid", String.valueOf(mCheckError.getMjjid()));
//            if (mCheckErrorOld != null) {
//                mCheckErrorOld.setAnchor(mCheckError.getAnchor());
//                mCheckErrorOld.setId(mCheckError.getId());
//                mCheckErrorOld.setPdid(mCheckError.getPdid());
//                mCheckErrorOld.setStatus(9);
//                saveList.add(mCheckErrorOld);
//            } else {
//                mCheckError.setStatus(9);
//                newList.add(mCheckError);
//            }
//        }
        if (newList != null && !newList.isEmpty()) {
            DBDataUtils.saveAll(newList);
        }
        if (saveList != null && !saveList.isEmpty()) {
            DBDataUtils.updateAll(saveList);
        }
//        mhandler.obtainMessage(Constant.BackThread_SUCCESS).sendToTarget();
    }
}
