package com.botongsoft.rfid.ui.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.botongsoft.rfid.R;
import com.botongsoft.rfid.bean.JsonBean.CountJson;
import com.botongsoft.rfid.bean.classity.CheckError;
import com.botongsoft.rfid.bean.classity.CheckPlan;
import com.botongsoft.rfid.bean.classity.CheckPlanDeatil;
import com.botongsoft.rfid.bean.classity.CheckPlanDeatilDel;
import com.botongsoft.rfid.bean.classity.Epc;
import com.botongsoft.rfid.bean.classity.Kf;
import com.botongsoft.rfid.bean.classity.LogDetail;
import com.botongsoft.rfid.bean.classity.LogMain;
import com.botongsoft.rfid.bean.classity.Mjj;
import com.botongsoft.rfid.bean.classity.Mjjg;
import com.botongsoft.rfid.bean.classity.Mjjgda;
import com.botongsoft.rfid.bean.classity.ServerLogRecord;
import com.botongsoft.rfid.bean.http.BaseResponse;
import com.botongsoft.rfid.busines.FilesBusines;
import com.botongsoft.rfid.common.constants.Constant;
import com.botongsoft.rfid.common.db.CheckDetailSearchDb;
import com.botongsoft.rfid.common.db.DBDataUtils;
import com.botongsoft.rfid.common.db.LogDbHelper;
import com.botongsoft.rfid.common.service.http.BusinessException;
import com.botongsoft.rfid.common.service.http.BusinessResolver;
import com.botongsoft.rfid.common.service.http.NetUtils;
import com.botongsoft.rfid.common.service.http.RequestTask;
import com.botongsoft.rfid.common.utils.ToastUtils;
import com.botongsoft.rfid.ui.Thread.WriteCheckDetailDBDelThread;
import com.botongsoft.rfid.ui.Thread.WriteCheckDetailDBThread;
import com.botongsoft.rfid.ui.Thread.WriteCheckErrorDBThread;
import com.botongsoft.rfid.ui.Thread.WriteCheckPlanDBThread;
import com.botongsoft.rfid.ui.Thread.WriteDetailLogDBThread;
import com.botongsoft.rfid.ui.Thread.WriteEpcDBThread;
import com.botongsoft.rfid.ui.Thread.WriteKfDBThread;
import com.botongsoft.rfid.ui.Thread.WriteMainLogDBThread;
import com.botongsoft.rfid.ui.Thread.WriteMjgDBThread;
import com.botongsoft.rfid.ui.Thread.WriteMjgDaDBThread;
import com.botongsoft.rfid.ui.Thread.WriteMjgDaDelDBThread;
import com.botongsoft.rfid.ui.Thread.WriteMjjDBThread;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.util.LogUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.botongsoft.rfid.R.id.appBarLayout;
import static com.botongsoft.rfid.R.id.toolbar;
import static com.botongsoft.rfid.common.constants.Constant.BackThread_PUTDETAILLOG_SUCCESS_PB;
import static com.botongsoft.rfid.common.constants.Constant.BackThread_PUT_CHECKDETAIL_SUCCESS_PB;
import static com.botongsoft.rfid.common.db.ServerLogRecordDbUtil.getServerLogRecordType;

/**
 * Created by pc on 2017/7/10.
 */

public class SyncbakActivity extends BaseActivity {
    private static final int HAS_NEW_MJJG = 8888;
    private static final int HAS_NEW_DA = 8889;
    private static final int HAS_NEW_EPC = 8890;
    private Activity mContext;
    @BindView(appBarLayout)
    AppBarLayout mAppBarLayout;
    @BindView(toolbar)
    Toolbar mToolbar;

    @BindView(R.id.tv_name1)
    TextView tv_name1;
    @BindView(R.id.bt_action1)
    Button bt_action1;
    @BindView(R.id.tv_oleNsize1)
    TextView tv_oleNsize1;
    @BindView(R.id.tv_status1)
    TextView tv_status1;
    @BindView(R.id.pb1)
    ProgressBar pb1;

    @BindView(R.id.tv_name2)
    TextView tv_name2;
    @BindView(R.id.bt_action2)
    Button bt_action2;
    @BindView(R.id.tv_oleNsize2)
    TextView tv_oleNsize2;
    @BindView(R.id.tv_status2)
    TextView tv_status2;
    @BindView(R.id.pb2)
    ProgressBar pb2;

    @BindView(R.id.tv_name3)
    TextView tv_name3;
    @BindView(R.id.bt_action3)
    Button bt_action3;
    @BindView(R.id.tv_oleNsize3)
    TextView tv_oleNsize3;
    @BindView(R.id.tv_status3)
    TextView tv_status3;
    @BindView(R.id.pb3)
    ProgressBar pb3;
    @BindView(R.id.tv_name4)
    TextView tv_name4;
    @BindView(R.id.bt_action4)
    Button bt_action4;
    @BindView(R.id.tv_oleNsize4)
    TextView tv_oleNsize4;
    @BindView(R.id.tv_status4)
    TextView tv_status4;
    @BindView(R.id.pb4)
    ProgressBar pb4;


    //checkplan
    @BindView(R.id.tv_name5)
    TextView tv_name5;
    @BindView(R.id.bt_action5)
    Button bt_action5;
    @BindView(R.id.tv_oleNsize5)
    TextView tv_oleNsize5;
    @BindView(R.id.tv_status5)
    TextView tv_status5;
    @BindView(R.id.pb5)
    ProgressBar pb5;

    //checkerror checkdetail
    @BindView(R.id.tv_name6)
    TextView tv_name6;
    @BindView(R.id.bt_action6)
    Button bt_action6;
    @BindView(R.id.tv_oleNsize6)
    TextView tv_oleNsize6;
    @BindView(R.id.tv_status6)
    TextView tv_status6;
    @BindView(R.id.pb6)
    ProgressBar pb6;

    //    @BindView(R.id.tv_name7)
    //    TextView tv_name7;
    //    @BindView(R.id.bt_action7)
    //    Button bt_action7;
    //    @BindView(R.id.tv_oleNsize7)
    //    TextView tv_oleNsize7;
    //    @BindView(R.id.tv_status7)
    //    TextView tv_status7;
    //    @BindView(R.id.pb7)
    //    ProgressBar pb7;

    //Epc  8
    @BindView(R.id.tv_name8)
    TextView tv_name8;
    @BindView(R.id.bt_action8)
    Button bt_action8;
    @BindView(R.id.tv_oleNsize8)
    TextView tv_oleNsize8;
    @BindView(R.id.tv_status8)
    TextView tv_status8;
    @BindView(R.id.pb8)
    ProgressBar pb8;

    //log
    @BindView(R.id.tv_name9)
    TextView tv_name9;
    @BindView(R.id.bt_action9)
    Button bt_action9;
    @BindView(R.id.tv_oleNsize9)
    TextView tv_oleNsize9;
    @BindView(R.id.tv_status9)
    TextView tv_status9;
    @BindView(R.id.pb9)
    ProgressBar pb9;

    private static final int CONN_SUCCESS = 0;
    private static final int CONN_UNSUCCESS = 1;
    private static final int CONN_UNSUCCESS1 = 3;
    private static final int INIT_DOWORK = 2;
    private static final int PUT_WROK_KF = 1002;
    private boolean isOnLine = true;//是否在线
    private boolean isOnScreen;//是否在屏幕上
    private HandlerThread mCheckMsgThread;//Handler线程池
    //后台运行的handler
    private Handler mCheckMsgHandler;
    //与UI线程管理的handler
    private Handler mHandler;
    private static final int BackThread_DOWORK = 9999;
    private static final int BackThread_GETKF = 1000;
    private static final int BackThread_GETMJJ = 1003;
    private static final int BackThread_GETMJJG = 1004;
    private static final int BackThread_GETMJJGDA = 1005;
    private static final int BackThread_PUTMJJGDA = 1006;
    private static final int BackThread_GETCHECKPLAN = 1007;
    private static final int BackThread_PUTCHECKERRORPLAN = 1008;
    private static final int BackThread_PUTCHECKDETAILPLAN = 1009;
    private static final int BackThread_GETEPC = 1010;
    private static final int BackThread_PUTLOG = 1011;
    private static final int BackThread_PUTLOGDETAIL = 1012;
    //传递后台运行消息队列
    private Message backThreadmsg;
    private Message uiMsg;
    private Thread networkThread;//网络操作相关的子线程
    private WriteKfDBThread wrKfDbThread;//数据库操作相关
    private WriteMjjDBThread wrMjjDbThread;//数据库操作相关
    private WriteMjgDBThread wrMjgDbThread;//数据库操作相关
    private WriteMjgDaDBThread writeMjgDaDBThread;//数据库操作相关
    private WriteMjgDaDelDBThread writeMjgDaDelDBThread;//数据库操作相关
    private WriteCheckPlanDBThread writeCheckPlanDBThread;
    private WriteCheckErrorDBThread writeCheckErrorDBThread;
    private WriteCheckDetailDBThread writeCheckDetailDBThread;
    private WriteCheckDetailDBDelThread writeCheckDetailDBDelThread;
    private WriteMainLogDBThread writeMainLogDBThread;
    private WriteDetailLogDBThread writeDetailLogDBThread;
    private WriteEpcDBThread wrEpcDbThread;//数据库操作相关

    private static int temple = 0;//服务器更新的档案条目数
    private Long kfAnchor;
    private Long mjjAnchor;
    private Long mjjgAnchor;
    private Long mjjgdaAnchor;
    private Long checkPlanAnchor;
    private volatile Long epcAnchor;
    private Long serverCheckDetailAnchor = 0L;
    private Long serverCheckErrorAnchor = 0L;
    private long mCheckDetailCount;//盘点错误记录本地提交服务器总数量=提交数据量+删除数量 mCheckDetailCount = CheckPlanDeatilCount+CheckPlanDeatilDelCount
    private int CheckPlanDeatilDelCount;//盘点错误记录本地提交服务器 提交数据量
    private int CheckPlanDeatilCount;//盘点错误记录本地提交服务器 删除数量
    private long mCheckErrorCount;//盘点格子记录本地提交服务器数量
    private long mCheckErrorDetailCount;//盘点记录与错误记录总数量 mCheckErrorDetailCount =  mCheckDetailCount+mCheckErrorCount
    private long mDaLocalCount;//档案本地提交服务器总数量 = 已同步被删除数+新增数
    private int DaDelCount;//档案本地提交服务器已同步被删除数
    private int DaNewCount;//档案本地提交服务器新增
    private int logMainCount;
    private int logDetailCount;
    private int mlogCount;//本地提交服务器日志数量；
    private RequestTask task;
    private boolean getKfFlag = false;
    private boolean getMjjFlag = false;
    private boolean getMjgflag = false;
    private boolean getDaFLag = false;
    private boolean putDaFLag = false;
    private boolean getCheckPlanFLag = false;
    private boolean putCheckDetailFLag = false;
    private boolean putCheckErrorFLag = false;
    private boolean getEpcFlag = false;
    private boolean putLogFLag = false;
    private boolean isPause;
    private List<Mjjgda> getMjjgdaJsonList;
    private List<Mjjgda> putMjjgdaJsonList;
    private List<Mjjg> mjjgJsonList;
    private List<Mjj> mjjJsonList;
    private List<Kf> kfJsonList;
    private List<Epc> epcJsonList;
    private List<CheckPlan> checkPlanJsonList;
    private List<CheckError> checkErrorJsonList;
    private List<CheckPlanDeatil> checkDetailJsonList;
    private List<LogMain> logMainJsonList;
    private List<LogDetail> logDetailJsonList;

    private final int limit = 500;
    private int checkdetailCountTemp = 0;
    private int checkerrorCountTemp = 0;
    private int putNewDaCountTemp = 0;
    private int putNewDetailLogCountTemp = 0;
    private Mjjg mjjgInfo;
    private Mjjgda mjjgdaInfo;
    private Epc epcInfo;
    private List<Mjjgda> getMjjgdaDELJsonList;
    private int delDaLogId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_sync_bak);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);
        mContext = this;
        initUiHandler();
        initDatas();
    }

    private void initUiHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle data = msg.getData();
                switch (msg.what) {
                    case CONN_SUCCESS:
                        backThreadmsg = mCheckMsgHandler.obtainMessage();
                        LogUtils.d("BackThread_DOWORK;");
                        backThreadmsg.what = BackThread_DOWORK;
                        mCheckMsgHandler.sendMessage(backThreadmsg);
                        break;
                    case INIT_DOWORK:
                        FilesBusines.getWorkState(mContext, (BusinessResolver.BusinessCallback<BaseResponse>) mContext, kfAnchor, mjjAnchor, mjjgAnchor, mjjgdaAnchor, checkPlanAnchor, epcAnchor, delDaLogId);
                        break;
                    case CONN_UNSUCCESS:
                        new AlertDialog.Builder(mContext)
                                .setTitle("服务器无法访问")
                                .setMessage("情检查网络是否畅通")
                                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {

                                    }
                                })
                                .create().show();
                        bt_action1.setEnabled(false);
                        bt_action2.setEnabled(false);
                        bt_action3.setEnabled(false);
                        bt_action4.setEnabled(false);
                        bt_action5.setEnabled(false);
                        bt_action6.setEnabled(false);
                        //                        bt_action7.setEnabled(false);
                        bt_action8.setEnabled(false);
                        bt_action9.setEnabled(false);
                        isOnLine = false;
                        break;
                    case CONN_UNSUCCESS1:
                        ToastUtils.showShort("网络中断");
                        //                        task.cancel(true);
                        //                        if (networkThread != null) {
                        //                            networkThread.interrupt();//中断线程的方法
                        //                            networkThread = null;
                        //                        }
                        break;
                    case Constant.BackThread_SUCCESS:
                        ToastUtils.showLong("通知界面保存完毕");

                        break;
                    case Constant.BackThread_PUTMAINLOG_SUCCESS_PB:
                        //                        if(logMainJsonList.size()==0){
                        //                            mCheckMsgHandler.obtainMessage(BackThread_PUTLOGDETAIL).sendToTarget();
                        //                            putLogFLag = false;
                        //                        }else {
                        int log = data.getInt("log");
                        pb9.setMax(logMainJsonList.size());
                        pb9.setProgress(log);
                        tv_status9.setText("正在提交主日志记录");
                        tv_status9.setTextColor(Color.RED);
                        if (log == logMainJsonList.size()) {
                            tv_status9.setText("准备提交日志明细记录");
                            mCheckMsgHandler.obtainMessage(BackThread_PUTLOGDETAIL).sendToTarget();
                            putLogFLag = false;
                        }
                        //                        }


                        break;
                    case BackThread_PUTDETAILLOG_SUCCESS_PB:
                        if (logDetailJsonList != null && logDetailJsonList.size() > 0) {
                            int detaillog = data.getInt("detaillog");
                            pb9.setMax(logDetailJsonList.size());
                            pb9.setProgress(detaillog);
                            tv_status9.setText("正在提交日志明细记录");
                            tv_status9.setTextColor(Color.RED);
                            if (detaillog == logDetailJsonList.size()) {
                                putNewDetailLogCountTemp += limit;
                                if ((logDetailCount - putNewDetailLogCountTemp) >= 0) {
                                    //总数-临时数大于0 说明还有未提交的数据，继续执行put的方法上传数据
                                    putLogFLag = false;
                                    mCheckMsgHandler.obtainMessage(BackThread_PUTLOGDETAIL).sendToTarget();
                                }
                                if (putNewDetailLogCountTemp >= logDetailCount) {
                                    //如果最后统计数大于等于初始显示的数量 提示更新完成
                                    tv_oleNsize9.setText("更新完成");
                                    tv_oleNsize9.setTextColor(Color.GREEN);
                                    tv_status9.setText("");
                                    putLogFLag = false;
                                }
                            }
                        }

                        break;
                    case BackThread_PUT_CHECKDETAIL_SUCCESS_PB:

                        int checkdetail = data.getInt("checkdetail");//Thread传出的数据消息
                        int checkdetaildel = data.getInt("checkdetaildel");
                        if (checkdetail > 0) {
                            LogUtils.d("BackThread_PUT_CHECKDETAIL_SUCCESS_PB--> " + checkdetail);
                            pb6.setMax(checkDetailJsonList.size());
                            pb6.setProgress(checkdetail);
                            tv_status6.setText("正在写入数据库");
                            tv_status6.setTextColor(Color.RED);
                            //如果进度条等于服务器返回的数据大小。说明该次的进度已经结束
                            if (checkdetail == checkDetailJsonList.size()) {
                                checkdetailCountTemp += limit;
                                if ((CheckPlanDeatilCount - checkdetailCountTemp) >= 0) {
                                    //总数-临时数大于0 说明还有未提交的数据，继续执行put的方法上传数据
                                    putCheckDetailFLag = false;
                                    mCheckMsgHandler.obtainMessage(BackThread_PUTCHECKDETAILPLAN).sendToTarget();
                                }
                                if (checkdetailCountTemp >= CheckPlanDeatilCount) {
                                    //如果最后统计数大于等于初始显示的数量 提示更新完成
                                    tv_oleNsize6.setText("更新完成");
                                    tv_oleNsize6.setTextColor(Color.GREEN);
                                    tv_status6.setText("");
                                    putCheckDetailFLag = false;
                                }
                            }
                        } else if (checkdetail == 0 && CheckPlanDeatilDelCount > 0) {
                            //如果本地盘点明细无数据 但是删除有数据
                            pb6.setMax(CheckPlanDeatilDelCount);
                            pb6.setProgress(checkdetaildel);
                            tv_status6.setText("正在写入数据库");
                            tv_status6.setTextColor(Color.RED);
                            if (checkdetaildel == CheckPlanDeatilDelCount) {
                                tv_oleNsize6.setText("更新完成");
                                tv_oleNsize6.setTextColor(Color.GREEN);
                                tv_status6.setText("");
                                putCheckDetailFLag = false;
                                putCheckErrorFLag = false;
                            }
                        } else {
                            tv_oleNsize6.setText("更新完成");
                            tv_oleNsize6.setTextColor(Color.GREEN);
                            tv_status6.setText("");
                            putCheckDetailFLag = false;
                            putCheckErrorFLag = false;
                        }

                        break;
                    case Constant.BackThread_PUT_CHECKERROR_SUCCESS_PB:
                        int checkerror = data.getInt("checkerror");
                        LogUtils.d("BackThread_PUT_CHECKERROR_SUCCESS_PB--> " + checkerror);
                        pb6.setMax(checkErrorJsonList.size());
                        pb6.setProgress(checkerror);
                        tv_status6.setText("正在写入数据库");
                        tv_status6.setTextColor(Color.RED);
                        if (checkerror == checkErrorJsonList.size()) {
                            checkerrorCountTemp += limit;
                            if ((mCheckErrorCount - checkerrorCountTemp) >= 0) {
                                //总数-临时数大于0 说明还有未提交的数据，继续执行put的方法上传数据
                                putCheckErrorFLag = false;
                                mCheckMsgHandler.obtainMessage(BackThread_PUTCHECKERRORPLAN).sendToTarget();
                            }
                            if (checkerrorCountTemp >= mCheckErrorCount) {
                                //如果最后统计数大于等于初始显示的数量 提示更新完成
                                tv_oleNsize6.setText("更新完成");
                                tv_oleNsize6.setTextColor(Color.GREEN);
                                tv_status6.setText("");
                                putCheckErrorFLag = false;
                                //主数据提交后在提交明细
                                putCheckDetailFLag = false;
                                tv_status6.setText("准备提交明细记录");
                                mCheckMsgHandler.obtainMessage(BackThread_PUTCHECKDETAILPLAN).sendToTarget();
                            }
                            //                            tv_oleNsize6.setText("更新完成");
                            //                            tv_oleNsize6.setTextColor(Color.GREEN);
                            //                            tv_status6.setText("");
                            //                            putCheckErrorFLag = false;
                        }
                        break;
                    case Constant.BackThread_GETCHECKPLAN_SUCCESS_PB:
                        int checkplan = data.getInt("checkplan");
                        LogUtils.d("BackThread_GETCHECKPLAN_SUCCESS_PB--> " + checkplan);
                        pb5.setMax(checkPlanJsonList.size());
                        pb5.setProgress(checkplan);
                        tv_status5.setText("正在写入数据库");
                        tv_status5.setTextColor(Color.RED);
                        if (checkplan == checkPlanJsonList.size()) {
                            tv_oleNsize5.setText("更新完成");
                            tv_oleNsize5.setTextColor(Color.GREEN);
                            tv_status5.setText("");
                            getCheckPlanFLag = false;
                        }
                        break;
                    case Constant.BackThread_GETDA_SUCCESS_PB:
                        int da = data.getInt("da");
                        LogUtils.d("BackThread_GETDA_SUCCESS_PB-->GetServer " + da);
                        LogUtils.d(getMjjgdaJsonList.size() + "");
                        pb4.setMax(getMjjgdaJsonList.size());
                        pb4.setProgress(da);
                        tv_status4.setText("正在写入数据库");
                        tv_status4.setTextColor(Color.RED);
                        if (da == getMjjgdaJsonList.size()) {

                            //第一批次的写进数据库后 再次取得最新的版本号提交服务器取得新一批的数据
                            //                            mCheckMsgHandler.obtainMessage(HAS_NEW_DA).sendToTarget();
                            backThreadmsg = mCheckMsgHandler.obtainMessage();
                            backThreadmsg.what = HAS_NEW_DA;
                            mCheckMsgHandler.sendMessageDelayed(backThreadmsg, 500);
                            //                            tv_oleNsize4.setText("更新完成");
                            //                            tv_oleNsize4.setTextColor(Color.GREEN);
                            //                            tv_status4.setText("");
                            //                            if (mDaLocalCount > 0) {
                            //                                //接收完服务器数据后再上传本地的数据
                            //                                mCheckMsgHandler.obtainMessage(BackThread_PUTMJJGDA).sendToTarget();
                            //                            }
                            getDaFLag = false;
                        }
                        temple = 0;

                        break;
                    case Constant.BackThread_PUTDA_SUCCESS_PB:
                        int putda = data.getInt("da");
                        int putdadel = data.getInt("dadel");
                        if (putdadel > 0 && putda == 0) {
                            //如果只有提交删除数据没有提交上架数据 进度条值走删除记录的进度条
                            LogUtils.d("BackThread_PUTDA_SUCCESS_PB-->PutServerDel 档案DEL" + putdadel);
                            pb4.setMax(DaDelCount);
                            pb4.setProgress(putdadel);
                            tv_status4.setText("正在写入数据库");
                            tv_status4.setTextColor(Color.RED);
                            if (putdadel == DaDelCount) {
                                tv_oleNsize4.setText("更新完成");
                                tv_oleNsize4.setTextColor(Color.GREEN);
                                tv_status4.setText("");
                                putDaFLag = false;
                                temple = 0;
                            }
                        } else {
                            LogUtils.d("BackThread_PUTDA_SUCCESS_PB-->PutServer 档案NEW" + putda);
                            pb4.setMax(putMjjgdaJsonList.size());
                            pb4.setProgress(putda);
                            tv_status4.setText("正在写入数据库");
                            tv_status4.setTextColor(Color.RED);
                            if (putda == putMjjgdaJsonList.size()) {
                                putNewDaCountTemp += limit;
                                if ((DaNewCount - putNewDaCountTemp) >= 0) {
                                    //总数-临时数大于0 说明还有未提交的数据，继续执行put的方法上传数据
                                    putDaFLag = false;
                                    mCheckMsgHandler.obtainMessage(BackThread_PUTMJJGDA).sendToTarget();
                                }
                                if (putNewDaCountTemp >= DaNewCount) {
                                    //如果最后统计数大于等于初始显示的数量 提示更新完成
                                    tv_oleNsize4.setText("更新完成");
                                    tv_oleNsize4.setTextColor(Color.GREEN);
                                    tv_status4.setText("");
                                    putDaFLag = false;
                                    temple = 0;
                                }
                                //                                tv_oleNsize4.setText("更新完成");
                                //                                tv_oleNsize4.setTextColor(Color.GREEN);
                                //                                tv_status4.setText("");
                                //                                putDaFLag = false;
                                //                                temple = 0;
                            }
                        }
                        break;
                    case Constant.BackThread_GETEPC_SUCCESS_PB:
                        int epc = data.getInt("epc");
                        LogUtils.d("BackThread_GETMJG_SUCCESS_PB--> Get密集格进度条" + epc);
                        pb8.setMax(epcJsonList.size());
                        pb8.setProgress(epc);
                        tv_status8.setText("正在写入数据库");
                        tv_status8.setTextColor(Color.RED);
                        if (epc == epcJsonList.size()) {
                            //第一批次的写进数据库后 再次取得最新的版本号提交服务器取得新一批的数据
                            //                            mCheckMsgHandler.obtainMessage(HAS_NEW_EPC).sendToTarget();
                            backThreadmsg = mCheckMsgHandler.obtainMessage();
                            backThreadmsg.what = HAS_NEW_EPC;
                            mCheckMsgHandler.sendMessageDelayed(backThreadmsg, 500);

                            //                            tv_oleNsize3.setText("更新完成");
                            //                            tv_oleNsize3.setTextColor(Color.GREEN);
                            //                            tv_status3.setText("");
                            getEpcFlag = false;
                        }
                        break;
                    case Constant.BackThread_GETMJG_SUCCESS_PB:
                        int mjjg = data.getInt("mjg");
                        LogUtils.d("BackThread_GETMJG_SUCCESS_PB--> Get密集格进度条" + mjjg);
                        pb3.setMax(mjjgJsonList.size());
                        pb3.setProgress(mjjg);
                        tv_status3.setText("正在写入数据库");
                        tv_status3.setTextColor(Color.RED);
                        if (mjjg == mjjgJsonList.size()) {
                            //第一批次的写进数据库后 再次取得最新的版本号提交服务器取得新一批的数据
                            //                            mCheckMsgHandler.obtainMessage(HAS_NEW_MJJG).sendToTarget();

                            backThreadmsg = mCheckMsgHandler.obtainMessage();
                            backThreadmsg.what = HAS_NEW_MJJG;
                            mCheckMsgHandler.sendMessageDelayed(backThreadmsg, 500);

                            //                            tv_oleNsize3.setText("更新完成");
                            //                            tv_oleNsize3.setTextColor(Color.GREEN);
                            //                            tv_status3.setText("");
                            getMjgflag = false;
                        }
                        break;
                    case Constant.BackThread_GETMJJ_SUCCESS_PB:
                        int mjj = data.getInt("mjj");
                        LogUtils.d(mjj + "");
                        pb2.setMax(mjjJsonList.size());
                        pb2.setProgress(mjj);
                        tv_status2.setText("正在写入数据库");
                        tv_status2.setTextColor(Color.RED);
                        if (mjj == mjjJsonList.size()) {
                            tv_oleNsize2.setText("更新完成");
                            tv_oleNsize2.setTextColor(Color.GREEN);
                            tv_status2.setText("");
                            getMjjFlag = false;
                        }
                        break;
                    case Constant.BackThread_GETKF_SUCCESS_PB:
                        int kf = data.getInt("kf");
                        LogUtils.d(kf + "");
                        pb1.setMax(kfJsonList.size());
                        pb1.setProgress(kf);
                        tv_status1.setText("正在写入数据库");
                        tv_status1.setTextColor(Color.RED);
                        if (kf == kfJsonList.size()) {
                            tv_oleNsize1.setText("更新完成");
                            tv_oleNsize1.setTextColor(Color.GREEN);
                            tv_status1.setText("");
                            getKfFlag = false;
                        }
                        break;
                    default:
                        super.handleMessage(msg);
                        break;
                }
            }

        };
    }

    private void initDatas() {
        networkThread = new Thread(networkTask);
        networkThread.start();
        if (mCheckMsgThread == null || !mCheckMsgThread.isAlive()) {
            mCheckMsgThread = new HandlerThread("BackThread");// 创建一个BackHandlerThread对象，它是一个线程
            mCheckMsgThread.start();// 启动线程
            initBackThread();
        }
    }

    @Override
    protected void initEvents() {
        tv_name1.setText("库房");
        tv_name2.setText("密集架");
        tv_name3.setText("密集格");
        tv_name4.setText("档案");
        tv_name5.setText("盘点计划");
        tv_name6.setText("盘点记录");
        //        tv_name7.setText("盘点纠错");
        tv_name8.setText("档号对照表");
        tv_name9.setText("日志记录");
    }

    @OnClick({R.id.bt_action1, R.id.bt_action2, R.id.bt_action3, R.id.bt_action4, R.id.bt_action5, R.id.bt_action6, R.id.bt_action8, R.id.bt_action9})
    public void click(Button button) {
        switch (button.getId()) {
            case R.id.bt_action1:
                bt_action1.setEnabled(false);
                action(BackThread_GETKF);
                break;
            case R.id.bt_action2:
                button.setEnabled(false);
                action(BackThread_GETMJJ);
                break;
            case R.id.bt_action3:
                button.setEnabled(false);
                action(BackThread_GETMJJG);
                break;
            case R.id.bt_action4:
                button.setEnabled(false);
                action4();
                break;
            case R.id.bt_action5:
                button.setEnabled(false);
                action(BackThread_GETCHECKPLAN);
                break;
            case R.id.bt_action6:
                button.setEnabled(false);
                action(BackThread_PUTCHECKERRORPLAN);
                break;
            //按钮7的整个视图打算取消
            //            case bt_action7:
            //     button.setEnabled(false);
            //                action(BackThread_PUTCHECKDETAILPLAN);
            //                break;
            case R.id.bt_action8:
                bt_action8.setEnabled(false);
                action(BackThread_GETEPC);
                break;
            case R.id.bt_action9:
                bt_action9.setEnabled(false);
                action9();
                break;
            default:
                break;
        }
    }

    private void action(int message) {
        backThreadmsg = mCheckMsgHandler.obtainMessage();
        backThreadmsg.what = message;
        mCheckMsgHandler.sendMessage(backThreadmsg);
    }

    //    private void action1() {
    //        backThreadmsg = mCheckMsgHandler.obtainMessage();
    //        LogUtils.d("BackThread_GETKF;");
    //        backThreadmsg.what = BackThread_GETKF;
    //        mCheckMsgHandler.sendMessage(backThreadmsg);
    //    }

    //    private void action2() {
    //        backThreadmsg = mCheckMsgHandler.obtainMessage();
    //        LogUtils.d("BackThread_GETMJJ;");
    //        backThreadmsg.what = BackThread_GETMJJ;
    //        mCheckMsgHandler.sendMessage(backThreadmsg);
    //    }

    //    private void action3() {
    //        backThreadmsg = mCheckMsgHandler.obtainMessage();
    //        LogUtils.d("BackThread_GETMJJG;");
    //        backThreadmsg.what = BackThread_GETMJJG;
    //        mCheckMsgHandler.sendMessage(backThreadmsg);
    //    }

    private void action4() {
        if (temple > 0) {
            //如果服务器有更新数据 先获取服务器的更新数据,然后在通知线程完毕后去查找本地是否有更新数据再上传到服务器
            backThreadmsg = mCheckMsgHandler.obtainMessage();
            backThreadmsg.what = BackThread_GETMJJGDA;
            mCheckMsgHandler.sendMessage(backThreadmsg);
        } else if (mDaLocalCount > 0 && temple <= 0) {
            //服务器没有更新，本地有更新
            mCheckMsgHandler.obtainMessage(BackThread_PUTMJJGDA).sendToTarget();
        }
    }

    //    private void action5() {
    //        backThreadmsg = mCheckMsgHandler.obtainMessage();
    //        LogUtils.d("BackThread_GETCHECKPLAN;");
    //        backThreadmsg.what = BackThread_GETCHECKPLAN;
    //        mCheckMsgHandler.sendMessage(backThreadmsg);
    //    }

    //    private void action6() {
    //        backThreadmsg = mCheckMsgHandler.obtainMessage();
    //        LogUtils.d("BackThread_PUTCHECKERRORPLAN;");
    //        backThreadmsg.what = BackThread_PUTCHECKERRORPLAN;
    //        mCheckMsgHandler.sendMessage(backThreadmsg);
    //    }

    //    private void action7() {
    //        backThreadmsg = mCheckMsgHandler.obtainMessage();
    //        LogUtils.d("BackThread_PUTCHECKDETAILPLAN;");
    //        backThreadmsg.what = BackThread_PUTCHECKDETAILPLAN;
    //        mCheckMsgHandler.sendMessage(backThreadmsg);
    //    }

    //    private void action8() {
    //        backThreadmsg = mCheckMsgHandler.obtainMessage();
    //        LogUtils.d("BackThread_GETEPC;");
    //        backThreadmsg.what = BackThread_GETEPC;
    //        mCheckMsgHandler.sendMessage(backThreadmsg);
    //    }

    private void action9() {
        backThreadmsg = mCheckMsgHandler.obtainMessage();
        LogUtils.d("BackThread_PUTLOG;");
        if (logDetailCount > 0 && logMainCount == 0) {//如果主日志已经上传，状态就都为9，查询不到。但是明细日志还有未上传的记录就直接进行明细上传，
            backThreadmsg.what = BackThread_PUTLOGDETAIL;
        } else {
            backThreadmsg.what = BackThread_PUTLOG;
        }

        mCheckMsgHandler.sendMessage(backThreadmsg);
    }


    @Override
    public void onSuccess(BaseResponse response, int act) {
        if (response != null) {
            if (act == BackThread_DOWORK) {//服务器返回更新数
                if (response.isSuccess()) {
                    backThread_DoWork(response);
                }
            } else if (act == BackThread_GETKF) {
                if (response.isSuccess()) {
                    backThread_GetKf(response);
                }
            } else if (act == BackThread_GETMJJ) {
                if (response.isSuccess()) {
                    backThread_GetMjj(response);
                }
            } else if (act == BackThread_GETMJJG) {
                if (response.isSuccess()) {
                    backThread_GetMjjg(response);
                }
            } else if (act == BackThread_GETMJJGDA) {
                if (response.isSuccess()) {
                    backThread_GetMjjDa(response);
                    //                    flag = false;
                }
            } else if (act == BackThread_PUTMJJGDA) {
                if (response.isSuccess()) {
                    backThread_PutMjjDa(response);
                    //                    flag = false;
                }
            } else if (act == BackThread_GETCHECKPLAN) {
                if (response.isSuccess()) {
                    backThread_GetCheckPlan(response);
                }
            } else if (act == BackThread_PUTCHECKERRORPLAN) {
                if (response.isSuccess()) {
                    backThread_PutCheckErrorPlan(response);
                }
            } else if (act == BackThread_PUTCHECKDETAILPLAN) {
                if (response.isSuccess()) {
                    backThread_PutCheckDetailPlan(response);
                }
            } else if (act == BackThread_GETEPC) {
                if (response.isSuccess()) {
                    backThread_GetEpc(response);
                }
            } else if (act == BackThread_PUTLOG) {
                if (response.isSuccess()) {
                    backThread_PutLog(response);
                }
            }
        }
    }

    private void backThread_DoWork(BaseResponse response) {
        try {
            List<CountJson> countJsons = JSONObject.parseArray(response.res.rows, CountJson.class);
            if (Integer.valueOf(countJsons.get(0).kf) > 0) {
                tv_oleNsize1.setText("服务器新数据：" + countJsons.get(0).kf + "条记录");
            } else {
                tv_oleNsize1.setText("无更新内容");
            }
            if (Integer.valueOf(countJsons.get(0).mjj) > 0) {
                tv_oleNsize2.setText("服务器新数据：" + countJsons.get(0).mjj + "条记录");
            } else {
                tv_oleNsize2.setText("无更新内容");
            }
            if (Integer.valueOf(countJsons.get(0).mjjg) > 0) {
                tv_oleNsize3.setText("服务器新数据：" + countJsons.get(0).mjjg + "条记录");
            } else {
                tv_oleNsize3.setText("无更新内容");
            }

            if (Integer.valueOf(countJsons.get(0).mjgda) > 0 && mDaLocalCount > 0) {
                temple = Integer.valueOf(countJsons.get(0).mjgda);
                StringBuilder sb = new StringBuilder();
                sb.append("本地有").append(mDaLocalCount).append("条数据需要提交").append("\n");
                sb.append("服务器新数据：").append(countJsons.get(0).mjgda).append("条记录");
                tv_oleNsize4.setText(sb.toString());
                //                            myBusinessInfos.get(3).setListSize("本地有" + mDaLocalCount + "条数据需要提交/服务器新数据：" + countJsons.get(0).mjgda + "条记录");
            } else if (Integer.valueOf(countJsons.get(0).mjgda) > 0 && mDaLocalCount == 0) {
                temple = Integer.valueOf(countJsons.get(0).mjgda);
                tv_oleNsize4.setText("服务器新数据：" + countJsons.get(0).mjgda + "条记录");
            } else if (Integer.valueOf(countJsons.get(0).mjgda) == 0 && mDaLocalCount > 0) {
                tv_oleNsize4.setText("本地有" + mDaLocalCount + "条数据需要提交");
            } else {
                tv_oleNsize4.setText("无更新内容");
            }

            if (Integer.valueOf(countJsons.get(0).checkplan) > 0) {
                tv_oleNsize5.setText("服务器新数据：" + countJsons.get(0).checkplan + "条记录");
            } else {
                tv_oleNsize5.setText("无更新内容");
            }

            if (mCheckErrorDetailCount > 0) {
                tv_oleNsize6.setText("本地有" + mCheckErrorDetailCount + "条数据需要提交");
            } else {
                tv_oleNsize6.setText("无更新内容");
            }
            /*
            if (mCheckDetailCount > 0) {
                tv_oleNsize7.setText("本地有" + mCheckDetailCount + "条数据需要提交");
            } else {
                tv_oleNsize7.setText("无更新内容");
            }*/
            //                        if (Long.valueOf(countJsons.get(0).checkErrorNum) > 0) {
            //                            serverCheckErrorAnchor = Long.valueOf(countJsons.get(0).checkErrorNum);
            //                        }
            //                        if (Long.valueOf(countJsons.get(0).checkDetailNum) > 0) {
            //                            serverCheckDetailAnchor = Long.valueOf(countJsons.get(0).checkDetailNum);
            //                        }

            if (Integer.valueOf(countJsons.get(0).epcNum) > 0) {
                tv_oleNsize8.setText("服务器新数据：" + countJsons.get(0).epcNum + "条记录");
            } else {
                tv_oleNsize8.setText("无更新内容");
            }
            if (mlogCount > 0) {
                tv_oleNsize9.setText("本地有" + mlogCount + "条数据需要提交");
            } else {
                tv_oleNsize9.setText("无更新内容");
            }
            if (Integer.valueOf(countJsons.get(0).delDaLogId) > 0) {
                ServerLogRecord sr = getServerLogRecordType(1);
                sr.setServerlogid(Integer.valueOf(countJsons.get(0).delDaLogId));
                DBDataUtils.update(sr);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void backThread_GetKf(BaseResponse response) {
        try {
            kfJsonList = JSON.parseArray(response.res.rows, Kf.class);
            if (kfJsonList != null && !kfJsonList.isEmpty()) {
                wrKfDbThread = new WriteKfDBThread(mHandler, uiMsg);
                wrKfDbThread.setList(kfJsonList);
                wrKfDbThread.start();
            } else {
                getKfFlag = false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void backThread_GetMjj(BaseResponse response) {
        try {
            mjjJsonList = JSON.parseArray(response.res.rows, Mjj.class);
            if (mjjJsonList != null && !mjjJsonList.isEmpty()) {
                wrMjjDbThread = new WriteMjjDBThread(mHandler, uiMsg);
                wrMjjDbThread.setList(mjjJsonList);
                wrMjjDbThread.start();
            } else {
                getMjjFlag = false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void backThread_GetMjjg(BaseResponse response) {
        try {
            mjjgJsonList = JSON.parseArray(response.res.rows, Mjjg.class);
            if (mjjgJsonList != null && !mjjgJsonList.isEmpty()) {
                wrMjgDbThread = new WriteMjgDBThread(mHandler, uiMsg);
                wrMjgDbThread.setList(mjjgJsonList);
                wrMjgDbThread.start();
            } else {
                tv_oleNsize3.setText("更新完成");
                tv_oleNsize3.setTextColor(Color.GREEN);
                tv_status3.setText("");
                getMjgflag = false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void backThread_PutMjjDa(BaseResponse response) {
        try {
            putMjjgdaJsonList = JSON.parseArray(response.res.rows, Mjjgda.class);
            if (putMjjgdaJsonList != null && !putMjjgdaJsonList.isEmpty()) {
                writeMjgDaDBThread = new WriteMjgDaDBThread(mHandler, uiMsg, 1);
                writeMjgDaDBThread.setList(putMjjgdaJsonList);
                writeMjgDaDBThread.start();
            } else {
                putDaFLag = false;
            }
            List<Mjjgda> delMjjgdaJsonList = JSON.parseArray(response.res.delrecords, Mjjgda.class);
            if (delMjjgdaJsonList != null && !delMjjgdaJsonList.isEmpty()) {
                writeMjgDaDelDBThread = new WriteMjgDaDelDBThread(mHandler, uiMsg, 1);
                writeMjgDaDelDBThread.setList(delMjjgdaJsonList);
                writeMjgDaDelDBThread.start();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void backThread_GetCheckPlan(BaseResponse response) {
        try {
            checkPlanJsonList = JSON.parseArray(response.res.rows, CheckPlan.class);
            if (checkPlanJsonList != null && !checkPlanJsonList.isEmpty()) {
                writeCheckPlanDBThread = new WriteCheckPlanDBThread(mHandler, uiMsg);
                writeCheckPlanDBThread.setList(checkPlanJsonList);
                writeCheckPlanDBThread.start();
            } else {
                getCheckPlanFLag = false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void backThread_PutCheckErrorPlan(BaseResponse response) {
        try {
            checkErrorJsonList = JSON.parseArray(response.res.rows, CheckError.class);
            if (checkErrorJsonList != null && !checkErrorJsonList.isEmpty()) {
                writeCheckErrorDBThread = new WriteCheckErrorDBThread(mHandler, uiMsg);
                writeCheckErrorDBThread.setList(checkErrorJsonList);
                writeCheckErrorDBThread.start();
            } else {
                putCheckErrorFLag = false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void backThread_PutCheckDetailPlan(BaseResponse response) {
        try {
            checkDetailJsonList = JSON.parseArray(response.res.rows, CheckPlanDeatil.class);
            if (checkDetailJsonList != null && !checkDetailJsonList.isEmpty()) {
                writeCheckDetailDBThread = new WriteCheckDetailDBThread(mHandler, uiMsg);
                writeCheckDetailDBThread.setList(checkDetailJsonList);
                writeCheckDetailDBThread.start();
            } else {
                putCheckDetailFLag = false;
            }
            List<CheckPlanDeatilDel> delJsonList = JSON.parseArray(response.res.delrecords, CheckPlanDeatilDel.class);
            if (delJsonList != null && !delJsonList.isEmpty()) {
                Log.i("delList", delJsonList.toString());
                writeCheckDetailDBDelThread = new WriteCheckDetailDBDelThread(mHandler, uiMsg);
                writeCheckDetailDBDelThread.setList(delJsonList);
                writeCheckDetailDBDelThread.start();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void backThread_GetEpc(BaseResponse response) {
        try {
            if (epcJsonList != null && epcJsonList.size() > 0) {
                epcJsonList.clear();
            }
            epcJsonList = JSON.parseArray(response.res.rows, Epc.class);
            if (epcJsonList != null && !epcJsonList.isEmpty()) {
                com.botongsoft.rfid.common.utils.LogUtils.w("epcanchor__backThread_GetEpc 我从服务器接收过来的第一条版本号", epcJsonList.get(0).getAnchor() + "" + " 线程名：" + String.valueOf(Thread.currentThread().getName()));
                com.botongsoft.rfid.common.utils.LogUtils.w("epcanchor__backThread_GetEpc 我从服务器接收过来的最后一条版本号", epcJsonList.get(epcJsonList.size() - 1).getAnchor() + "" + " 线程名：" + String.valueOf(Thread.currentThread().getName()));
                wrEpcDbThread = new WriteEpcDBThread(mHandler, uiMsg);
                wrEpcDbThread.setList(epcJsonList);
                wrEpcDbThread.start();
            } else {
                tv_oleNsize8.setText("更新完成");
                tv_oleNsize8.setTextColor(Color.GREEN);
                tv_status8.setText("");
                getEpcFlag = false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void backThread_GetMjjDa(BaseResponse response) {
        try {
            getMjjgdaDELJsonList = JSON.parseArray(response.res.delrecords, Mjjgda.class);
            if (getMjjgdaDELJsonList != null && !getMjjgdaDELJsonList.isEmpty()) {
                doDelDaByServer(response);
                doAddDaByServer(response);
            } else {
                doAddDaByServer(response);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void doDelDaByServer(BaseResponse response) {
        writeMjgDaDelDBThread = new WriteMjgDaDelDBThread(mHandler, uiMsg, 0);
        writeMjgDaDelDBThread.setList(getMjjgdaDELJsonList);
        writeMjgDaDelDBThread.start();
    }

    private void doAddDaByServer(BaseResponse response) {
        getMjjgdaJsonList = JSON.parseArray(response.res.rows, Mjjgda.class);
        if (getMjjgdaJsonList != null && !getMjjgdaJsonList.isEmpty()) {
            writeMjgDaDBThread = new WriteMjgDaDBThread(mHandler, uiMsg, 0);
            writeMjgDaDBThread.setList(getMjjgdaJsonList);
            writeMjgDaDBThread.start();
        } else {
            getDaFLag = false;
            if (mDaLocalCount > 0) {
                //接收完服务器数据后再上传本地的数据
                mCheckMsgHandler.obtainMessage(BackThread_PUTMJJGDA).sendToTarget();
            } else {
                tv_oleNsize4.setText("更新完成");
                tv_oleNsize4.setTextColor(Color.GREEN);
                tv_status4.setText("");
            }
        }
    }

    private void backThread_PutLog(BaseResponse response) {
        try {
            logMainJsonList = JSON.parseArray(response.res.rows, LogMain.class);
            if (logMainJsonList != null && !logMainJsonList.isEmpty()) {
                writeMainLogDBThread = new WriteMainLogDBThread(mHandler, uiMsg);
                writeMainLogDBThread.setList(logMainJsonList);
                writeMainLogDBThread.start();
            } else {
                putDaFLag = false;
            }
            logDetailJsonList = JSON.parseArray(response.res.detailrows, LogDetail.class);
            if (logDetailJsonList != null && !logDetailJsonList.isEmpty()) {
                Log.i("logDetailJsonList", logDetailJsonList.toString());
                writeDetailLogDBThread = new WriteDetailLogDBThread(mHandler, uiMsg);
                writeDetailLogDBThread.setList(logDetailJsonList);
                writeDetailLogDBThread.start();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(BusinessException e, int act) {
        ToastUtils.showLong(act + "");
        ToastUtils.showLong(e.getMessage());
    }


    private void initBackThread() {

        mCheckMsgHandler = new Handler(mCheckMsgThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                Log.w("Handler BackThread--->", String.valueOf(Thread.currentThread().getName()));
                switch (msg.what) {
                    case HAS_NEW_EPC:
                        //先将本地的版本号发送给服务器，服务器对比后返回大于这个版本号的数据进行更新本地库房表
                        backThread_Has_New_Epc();
                        break;
                    case HAS_NEW_MJJG:
                        //先将本地的版本号发送给服务器，服务器对比后返回大于这个版本号的数据进行更新本地库房表
                        backThread_Has_New_Mjjg();
                        break;
                    case HAS_NEW_DA:
                        //先将本地的版本号发送给服务器，判断服务器时候有更新过 有更新要解决冲突
                        backThread_Has_New_Da();
                        break;
                    case BackThread_DOWORK:
                        uiMsg = mHandler.obtainMessage();
                        uiMsg.what = INIT_DOWORK;
                        putBackAnchor();
                        mHandler.sendMessage(uiMsg);
                        //                        mHandler.obtainMessage(INIT_DOWORK).sendToTarget();
                        break;
                    case BackThread_GETKF:
                        backThread_Get_Kf();
                        break;
                    case BackThread_GETMJJ:
                        backThread_GetMjj();
                        break;
                    case BackThread_GETMJJG:
                        backThread_Get_Mjjg();
                        break;
                    case BackThread_GETMJJGDA:
                        backThread_GetDa();
                        break;
                    case BackThread_PUTMJJGDA:
                        backThread_Put_Da();
                        break;
                    case BackThread_GETCHECKPLAN:
                        backThread_Get_CheckPlan();
                        break;
                    case BackThread_PUTCHECKERRORPLAN:  //上传盘点过的格子记录
                        backThread_Put_CheckError_Plan();
                        break;
                    case BackThread_PUTCHECKDETAILPLAN:  //上传盘点纠错记录
                        backThread_Put_CheckDetail_Plan();
                        break;
                    case BackThread_GETEPC:
                        backThread_Get_Epc();
                        break;
                    case BackThread_PUTLOG:  //上传盘点纠错记录
                        backThread_PutLogMain();
                        break;
                    case BackThread_PUTLOGDETAIL:  //上传日志明细记录
                        backThread_Put_LogDetail();
                        break;
                    default:
                        super.handleMessage(msg);//这里最好对不需要或者不关心的消息抛给父类，避免丢失消息
                        break;
                }
            }

            private void backThread_Put_LogDetail() {
                if (putLogFLag) {
                    return;
                }
                isPause = false; // 防止多次点击下载,造成多个下载 flag = true;
                putLogFLag = true;
                List<LogDetail> logDetailList = (List<LogDetail>) LogDbHelper.getInfosHasOplimit(LogDetail.class, "status", "=", "0", "logid", "!=", "0", limit);
                if (logDetailList != null && logDetailList.size() > 0) {
                    FilesBusines.putLog(mContext, (BusinessResolver.BusinessCallback<BaseResponse>) mContext, BackThread_PUTLOG, null, logDetailList);
                } else {
                    uiMsg = mHandler.obtainMessage();
                    uiMsg.what = BackThread_PUTDETAILLOG_SUCCESS_PB;
                    mHandler.sendMessage(uiMsg);
                }
            }

            private void backThread_PutLogMain() {
                if (putLogFLag) {
                    return;
                }
                isPause = false; // 防止多次点击下载,造成多个下载 flag = true;
                putLogFLag = true;
                if (logDetailCount > 0 && logMainCount == 0) {//如果主日志已经上传，状态就都为9，查询不到。但是明细日志还有未上传的记录就直接进行明细上传，
                    List<LogDetail> logDetailList = (List<LogDetail>) LogDbHelper.getInfosHasOplimit(LogDetail.class, "status", "=", "0", limit);
                    FilesBusines.putLog(mContext, (BusinessResolver.BusinessCallback<BaseResponse>) mContext, BackThread_PUTLOG, null, logDetailList);
                } else {
                    List<LogMain> logMainList = (List<LogMain>) DBDataUtils.getInfosHasOp(LogMain.class, "status", "=", "0");//传全部记录

                    if (logMainList != null && logMainList.size() > 0) {
                        for (LogMain logMain : logMainList) {
                            int count = LogDbHelper.countDetail(logMain.getLid());
                            logMain.setSl(count);
                        }
                        FilesBusines.putLog(mContext, (BusinessResolver.BusinessCallback<BaseResponse>) mContext, BackThread_PUTLOG, logMainList, null);
                    } else {
                        putLogFLag = false;
                    }

                }
            }

            private void backThread_Get_Epc() {
                if (getEpcFlag) {
                    return;
                }
                isPause = false; // 防止多次点击下载,造成多个下载 flag = true;
                getEpcFlag = true;
                com.botongsoft.rfid.common.utils.LogUtils.w("epcanchor__getepc  我根据这个版本号去请求服务器比我大的数据", epcAnchor + "" + "线程名：" + String.valueOf(Thread.currentThread().getName()));
                FilesBusines.getState(mContext, (BusinessResolver.BusinessCallback<BaseResponse>) mContext, epcAnchor, BackThread_GETEPC);
            }

            private void backThread_Put_CheckDetail_Plan() {
                if (putCheckDetailFLag) {
                    return;
                }
                isPause = false; // 防止多次点击下载,造成多个下载 flag = true;
                putCheckDetailFLag = true;
                List<CheckPlanDeatil> checkDetailList = (List<CheckPlanDeatil>) CheckDetailSearchDb.getInfosHasOp(CheckPlanDeatil.class, "status", "=", "0", "anchor", ">", "0", limit);
                List<CheckPlanDeatilDel> checkDetailDelList = (List<CheckPlanDeatilDel>) DBDataUtils.getInfosHasOp(CheckPlanDeatilDel.class, "status", "=", "9", "anchor", ">", "0");
                if ((checkDetailList != null && checkDetailList.size() > 0) || (checkDetailDelList != null && checkDetailDelList.size() > 0)) {
                    FilesBusines.putCheckPlan(mContext, (BusinessResolver.BusinessCallback<BaseResponse>) mContext, BackThread_PUTCHECKDETAILPLAN, checkDetailList, null, checkDetailDelList);
                } else {
                    uiMsg = mHandler.obtainMessage();
                    uiMsg.what = BackThread_PUT_CHECKDETAIL_SUCCESS_PB;
                    mHandler.sendMessage(uiMsg);
                }
            }

            private void backThread_Put_CheckError_Plan() {
                if (putCheckErrorFLag) {
                    return;
                }
                isPause = false; // 防止多次点击下载,造成多个下载 flag = true;
                putCheckErrorFLag = true;
                if (mCheckDetailCount > 0 && mCheckErrorCount == 0) {
                    List<CheckPlanDeatil> checkDetailList = (List<CheckPlanDeatil>) CheckDetailSearchDb.getInfosHasOp(CheckPlanDeatil.class, "status", "=", "0", "anchor", ">", "0", limit);
                    List<CheckPlanDeatilDel> checkDetailDelList = (List<CheckPlanDeatilDel>) DBDataUtils.getInfosHasOp(CheckPlanDeatilDel.class, "status", "=", "9", "anchor", ">", "0");
                    FilesBusines.putCheckPlan(mContext, (BusinessResolver.BusinessCallback<BaseResponse>) mContext, BackThread_PUTCHECKDETAILPLAN, checkDetailList, null, checkDetailDelList);

                } else {
                    //                        List<CheckError> checkErrorList = (List<CheckError>) DBDataUtils.getInfosHasOp(CheckError.class, "status", "=", "0", "anchor", ">", "0");
                    //              打算把版本号为0的提交服务器，让服务器删除对应的明细数据
                    //  List<CheckError> checkErrorList = (List<CheckError>) CheckDetailSearchDb.getInfosHasOp(CheckError.class, "status", "=", "0", "anchor", ">", "0", limit);
                    List<CheckError> checkErrorList = (List<CheckError>) CheckDetailSearchDb.getInfosHasOpOr(CheckError.class, "status", "=", "0", "anchor", "=", "0", limit);
                    if (checkErrorList != null && checkErrorList.size() > 0) {
                        FilesBusines.putCheckPlan(mContext, (BusinessResolver.BusinessCallback<BaseResponse>) mContext, BackThread_PUTCHECKERRORPLAN, null, checkErrorList, null);
                    } else {
                        putCheckErrorFLag = false;
                    }
                }
            }

            private void backThread_Get_CheckPlan() {
                if (getCheckPlanFLag) {
                    return;
                }
                isPause = false; // 防止多次点击下载,造成多个下载 flag = true;
                getCheckPlanFLag = true;
                FilesBusines.getState(mContext, (BusinessResolver.BusinessCallback<BaseResponse>) mContext, checkPlanAnchor, BackThread_GETCHECKPLAN);
            }

            private void backThread_Put_Da() {
                if (putDaFLag) {
                    return;
                }
                isPause = false; // 防止多次点击下载,造成多个下载 flag = true;
                putDaFLag = true;
                //已同步被下架
                List<Mjjgda> tempList1 = (List<Mjjgda>) DBDataUtils.getInfosHasOp(Mjjgda.class, "status", "=", "-1", "anchor", ">", "0");
                //新上架子
                //                        List<Mjjgda> tempList = (List<Mjjgda>) DBDataUtils.getInfosHasOp(Mjjgda.class, "status", "=", "0", "anchor", "=", "0");
                List<Mjjgda> tempList = (List<Mjjgda>) CheckDetailSearchDb.getInfosHasOp(Mjjgda.class, "status", "=", "0", "anchor", "=", "0", limit);
                boolean st = NetUtils.isConnByHttp(Constant.DOMAINTEST());// 先判断对方服务器是否存在
                if (st) {
                    if ((tempList != null && !tempList.isEmpty()) || (tempList1 != null && !tempList1.isEmpty())) {
                        //                                task = new RequestTask((BusinessResolver.BusinessCallback<BaseResponse>) mContext, mContext);
                        FilesBusines.putDa(mContext, (BusinessResolver.BusinessCallback<BaseResponse>) mContext, BackThread_PUTMJJGDA, tempList, tempList1);
                    } else {
                        putDaFLag = false;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_oleNsize4.setText("更新完成");
                                tv_oleNsize4.setTextColor(Color.GREEN);
                                tv_status4.setText("");
                            }
                        });
                    }

                }
                //                        else {
                //                            if (task != null || task.getStatus() == AsyncTask.Status.RUNNING) {
                //                                task.cancel(true);
                //
                //                            }
                //                            break;
                //                        }
            }

            private void backThread_GetDa() {
                if (getDaFLag) {
                    return;
                }
                isPause = false; // 防止多次点击下载,造成多个下载 flag = true;
                getDaFLag = true;
                FilesBusines.getState(mContext, (BusinessResolver.BusinessCallback<BaseResponse>) mContext, mjjgdaAnchor, delDaLogId, BackThread_GETMJJGDA);
            }

            private void backThread_Get_Mjjg() {
                if (getMjgflag) {
                    return;
                }
                isPause = false; // 防止多次点击下载,造成多个下载 flag = true;
                getMjgflag = true;
                FilesBusines.getState(mContext, (BusinessResolver.BusinessCallback<BaseResponse>) mContext, mjjgAnchor, BackThread_GETMJJG);
            }

            private void backThread_GetMjj() {
                if (getMjjFlag) {
                    return;
                }
                isPause = false; // 防止多次点击下载,造成多个下载 flag = true;
                getMjjFlag = true;
                FilesBusines.getState(mContext, (BusinessResolver.BusinessCallback<BaseResponse>) mContext, mjjAnchor, BackThread_GETMJJ);
            }

            private void backThread_Get_Kf() {
                if (getKfFlag) {
                    return;
                }
                isPause = false; // 防止多次点击下载,造成多个下载 flag = true;
                getKfFlag = true;
                FilesBusines.getState(mContext, (BusinessResolver.BusinessCallback<BaseResponse>) mContext, kfAnchor, BackThread_GETKF);
            }

            private void backThread_Has_New_Da() {
                mjjgdaInfo = (Mjjgda) DBDataUtils.getInfoHasOp(Mjjgda.class, "anchor", ">=", "0");
                getDaLogId();
                if (mjjgdaInfo == null) {
                    mjjgdaAnchor = 0L;
                    LogUtils.d("da == getMjjgdaJsonList.size()" + mjjgdaAnchor);
                } else {
                    LogUtils.d("mjjgdaAnchor-->mjjgdaAnchor " + mjjgdaAnchor);
                    mjjgdaAnchor = Long.valueOf(mjjgdaInfo.getAnchor());
                    getDaFLag = false;
                    mCheckMsgHandler.obtainMessage(BackThread_GETMJJGDA).sendToTarget();
                }
            }

            private void backThread_Has_New_Mjjg() {
                mjjgInfo = (Mjjg) DBDataUtils.getInfoHasOp(Mjjg.class, "anchor", ">=", "0");
                if (mjjgInfo == null) {
                    mjjgAnchor = 0L;
                } else {
                    mjjgAnchor = Long.valueOf(mjjgInfo.getAnchor());
                    getMjgflag = false;
                    mCheckMsgHandler.obtainMessage(BackThread_GETMJJG).sendToTarget();
                }
            }

            private void backThread_Has_New_Epc() {
                com.botongsoft.rfid.common.utils.LogUtils.w("epcanchor__hasnew 当前内存里面的版本号", epcAnchor + "" + " 线程名：" + String.valueOf(Thread.currentThread().getName()));
                Epc epcInfo = (Epc) DBDataUtils.getInfoHasOp(Epc.class, "anchor", ">=", "0");
                if (epcInfo == null) {
                    epcAnchor = 0L;
                } else {
                    epcAnchor = Long.valueOf(epcInfo.getAnchor());
                    com.botongsoft.rfid.common.utils.LogUtils.w("epcanchor__hasnew 我要去取当前库里最大的版本号给服务器去查数据", epcAnchor + "" + " 线程名：" + String.valueOf(Thread.currentThread().getName()));
                    getEpcFlag = false;
                    mCheckMsgHandler.obtainMessage(BackThread_GETEPC).sendToTarget();
                }
            }

            private void putBackAnchor() {
                //先将本地的版本号发送给服务器，服务器对比后返回大于这个版本号的数据进行更新本地库房表
                Kf kfInfo = (Kf) DBDataUtils.getInfoHasOp(Kf.class, "anchor", ">=", "0");
                if (kfInfo == null) {
                    kfAnchor = 0L;
                } else {
                    kfAnchor = Long.valueOf(kfInfo.getAnchor());
                }
                //先将本地的版本号发送给服务器，服务器对比后返回大于这个版本号的数据进行更新本地库房表
                Mjj mjjInfo = (Mjj) DBDataUtils.getInfoHasOp(Mjj.class, "anchor", ">=", "0");
                if (mjjInfo == null) {
                    mjjAnchor = 0L;
                } else {
                    mjjAnchor = Long.valueOf(mjjInfo.getAnchor());
                }

                //先将本地的版本号发送给服务器，服务器对比后返回大于这个版本号的数据进行更新本地库房表
                Mjjg mjjgInfo1 = (Mjjg) DBDataUtils.getInfoHasOp(Mjjg.class, "anchor", ">=", "0");
                if (mjjgInfo1 == null) {
                    mjjgAnchor = 0L;
                } else {
                    mjjgAnchor = Long.valueOf(mjjgInfo1.getAnchor());
                }
                //先将本地的版本号发送给服务器，判断服务器时候有更新过 有更新要解决冲突
                Mjjgda mjjgdaInfo1 = (Mjjgda) DBDataUtils.getInfoHasOp(Mjjgda.class, "anchor", ">=", "0");
                if (mjjgdaInfo1 == null) {
                    mjjgdaAnchor = 0L;
                } else {
                    mjjgdaAnchor = Long.valueOf(mjjgdaInfo1.getAnchor());
                }
                try {
                    //已经同步过的数据下架了(版本号大于0 状态为删除状态-1)
                    DaDelCount = (int) DBDataUtils.count(Mjjgda.class, "status", "=", "-1", "anchor", ">", "0");
                    //新保存的上架记录
                    DaNewCount = (int) DBDataUtils.count(Mjjgda.class, "status", "=", "0", "anchor", "=", "0");
                    mDaLocalCount = DaDelCount + DaNewCount;
                    if (mDaLocalCount > 0) {
                        uiMsg.arg1 = (int) mDaLocalCount;
                    }
                } catch (DbException e) {
                    e.printStackTrace();
                }

                //盘点计划表版本号发送给服务器，服务器对比后返回大于这个版本号的数据进行更新本地库房表
                CheckPlan mCheckPlan = (CheckPlan) DBDataUtils.getInfoHasOp(CheckPlan.class, "anchor", ">=", "0");
                if (mCheckPlan == null) {
                    checkPlanAnchor = 0L;
                } else {
                    checkPlanAnchor = Long.valueOf(mCheckPlan.getAnchor());
                }
                //盘点日志明细表 盘点纠错表
                try {
                    int temp1 = (int) DBDataUtils.countOr(CheckError.class, "status", "=", "0", "anchor", "=", "0");
                    mCheckErrorCount = Long.valueOf(temp1);
                    CheckPlanDeatilCount = (int) DBDataUtils.count(CheckPlanDeatil.class, "status", "=", "0");
                    CheckPlanDeatilDelCount = (int) DBDataUtils.count(CheckPlanDeatilDel.class, "status", "=", "9", "anchor", ">", "0");
                    mCheckDetailCount = Long.valueOf(CheckPlanDeatilCount + CheckPlanDeatilDelCount);
                    mCheckErrorDetailCount = mCheckDetailCount + mCheckErrorCount;
                } catch (DbException e) {
                    e.printStackTrace();
                }
                /*
                //盘点纠错表
                try {
                    CheckPlanDeatilCount = (int) DBDataUtils.count(CheckPlanDeatil.class, "status", "=", "0", "anchor", ">=", "0");
                    CheckPlanDeatilDelCount = (int) DBDataUtils.count(CheckPlanDeatilDel.class, "status", "=", "9", "anchor", ">", "0");
                    mCheckDetailCount = Long.valueOf(CheckPlanDeatilCount + CheckPlanDeatilDelCount);
                } catch (DbException e) {
                    e.printStackTrace();
                }*/
                //先将本地的版本号发送给服务器，服务器对比后返回大于这个版本号的数据进行更新本地库房表
                Epc epcInfo = (Epc) DBDataUtils.getInfoHasOp(Epc.class, "anchor", ">=", "0");
                if (epcInfo == null) {
                    epcAnchor = 0L;
                } else {
                    epcAnchor = Long.valueOf(epcInfo.getAnchor());
                }
                com.botongsoft.rfid.common.utils.LogUtils.w("epcanchor__999 我只做第一次", epcAnchor + "" + " 线程名：" + String.valueOf(Thread.currentThread().getName()));
                //日志数量
                try {
                    logMainCount = (int) DBDataUtils.count(LogMain.class, "status", "=", "0");
                    logDetailCount = (int) DBDataUtils.count(LogDetail.class, "status", "=", "0");
                    mlogCount = logMainCount + logDetailCount;
                } catch (DbException e) {
                    e.printStackTrace();
                }
                getDaLogId();
            }
        };
    }

    private void getDaLogId() {
        ServerLogRecord sr = getServerLogRecordType(1);
        delDaLogId = sr.getServerlogid();
    }

    int mainLogId;//主记录ID 用来判断
    /**
     * 网络操作相关的子线程
     */

    Runnable networkTask = new Runnable() {

        @Override
        public void run() {
            // TODO
            // 在这里进行 http request.网络请求相关操作
            uiMsg = mHandler.obtainMessage();
            Bundle data = new Bundle();

            boolean st = NetUtils.isConnByHttp(Constant.DOMAINTEST());// 先判断对方服务器是否存在
            if (st) {
                uiMsg.what = CONN_SUCCESS;
            } else {
                uiMsg.what = CONN_UNSUCCESS;
            }
            mHandler.sendMessage(uiMsg);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.d("onResume");
        isOnScreen = true;
        //        if(isOnScreen && isRun) {
        if (isOnScreen) {

            //开启新进程
            if (mCheckMsgThread == null || !mCheckMsgThread.isAlive()) {
                mCheckMsgThread = new HandlerThread("BackThread");// 创建一个BackHandlerThread对象，它是一个线程
                mCheckMsgThread.start();// 启动线程
                //创建后台线程
                initBackThread();
            }

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtils.d("onPause");
        //停止查询
        isOnScreen = false;
        //        mCheckMsgThread.quit();
        //        if (task != null && task.getStatus() == AsyncTask.Status.RUNNING) {
        //            task.cancel(true);
        //        }

        //        temple = 0;
        //        mDaLocalCount = 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.d("onDestroy");
        //停止查询
        isOnScreen = false;
        temple = 0;
        mDaLocalCount = 0;
        //释放资源
        if (mCheckMsgHandler != null) {
            mCheckMsgThread.quit();
        }
        mCheckMsgHandler.removeCallbacksAndMessages(null);
        closeThread(networkThread);
        closeThread(wrKfDbThread);
        closeThread(wrMjjDbThread);
        closeThread(wrMjgDbThread);
        closeThread(writeMjgDaDBThread);
        closeThread(writeMjgDaDelDBThread);
        closeThread(writeCheckPlanDBThread);
        closeThread(writeCheckErrorDBThread);
        closeThread(writeCheckDetailDBThread);
        closeThread(writeCheckDetailDBDelThread);
        closeThread(writeMainLogDBThread);
        closeThread(writeDetailLogDBThread);
        closeThread(wrEpcDbThread);
        mCheckMsgHandler.removeCallbacksAndMessages(null);
        mHandler.removeCallbacksAndMessages(null);
        //        if (task != null && task.getStatus() == AsyncTask.Status.RUNNING) {
        //            task.cancel(true);
        //        }
        //        if (flag) {
        //            isPause = true;
        //            flag = false;
        //        }
        finish();
    }

    private void closeThread(Thread threadName) {
        if (threadName != null) {
            threadName.interrupt();//中断线程的方法
            threadName = null;
        }
    }

    /*
         * 重写返回键,模拟返回按下暂停键
         */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                LogUtils.d("KEYCODE_BACK");
                if (checkFlags()) return false;
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            LogUtils.d("onBackPressed");
            finishAfterTransition();
        } else {
            LogUtils.d("super onBackPressed");
            super.onBackPressed();
        }
    }

    @Override
    protected int getMenuID() {
        return R.menu.menu_book_detail;
    }

    MenuItem menuItem;


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                LogUtils.d("我按了返回键盘");
                if (checkFlags()) return false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finishAfterTransition();
                } else {
                    super.onBackPressed();
                }
                return true;
            case R.id.action_Sync:
                if (isOnLine) {//网络 状态正常master
                    //                    showAnimate(item); //这里开始动画 太丑了
                    item.setEnabled(false);
                    bt_action1.setEnabled(false);
                    mCheckMsgHandler.obtainMessage(BackThread_GETKF).sendToTarget();
                    bt_action2.setEnabled(false);
                    mCheckMsgHandler.obtainMessage(BackThread_GETMJJ).sendToTarget();
                    bt_action3.setEnabled(false);
                    mCheckMsgHandler.obtainMessage(BackThread_GETMJJG).sendToTarget();
                    bt_action4.setEnabled(false);
                    action4();
                    bt_action5.setEnabled(false);
                    mCheckMsgHandler.obtainMessage(BackThread_GETCHECKPLAN).sendToTarget();
                    bt_action6.setEnabled(false);
                    mCheckMsgHandler.obtainMessage(BackThread_PUTCHECKERRORPLAN).sendToTarget();
                    //                    bt_action7.setEnabled(false);
                    //                    mCheckMsgHandler.obtainMessage(BackThread_PUTCHECKDETAILPLAN).sendToTarget();
                    bt_action8.setEnabled(false);
                    mCheckMsgHandler.obtainMessage(BackThread_GETEPC).sendToTarget();
                    bt_action9.setEnabled(false);
                    mCheckMsgHandler.obtainMessage(BackThread_PUTLOG).sendToTarget();
                    //                    hideAnimate();
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean checkFlags() {
        if (getKfFlag == true) {
            ToastUtils.showToast("正在更新数据库，请勿返回", 500);
            return true;
        }
        if (getMjjFlag == true) {
            ToastUtils.showToast("正在更新数据库，请勿返回", 500);
            return true;
        }
        if (getMjgflag == true) {
            ToastUtils.showToast("正在更新数据库，请勿返回", 500);
            return true;
        }
        if (getDaFLag == true) {
            ToastUtils.showToast("正在更新数据库，请勿返回", 500);
            return true;
        }
        if (putDaFLag == true) {
            ToastUtils.showToast("正在更新数据库，请勿返回", 500);
            return true;
        }
        if (getCheckPlanFLag == true) {
            ToastUtils.showToast("正在更新数据库，请勿返回", 500);
            return true;
        }
        if (putCheckDetailFLag == true) {
            ToastUtils.showToast("正在更新数据库，请勿返回", 500);
            return true;
        }
        if (putCheckErrorFLag == true) {
            ToastUtils.showToast("正在更新数据库，请勿返回", 500);
            return true;
        }
        if (getEpcFlag == true) {
            ToastUtils.showToast("正在更新数据库，请勿返回", 500);
            return true;
        }
        //        if (putLogFLag == true) {
        //            ToastUtils.showToast("正在更新数据库，请勿返回", 500);
        //            return true;
        //        }
        return false;
    }

    /**
     * 关闭动画
     */
    private void hideAnimate() {
        if (menuItem != null) {
            View view = menuItem.getActionView();
            if (view != null) {
                view.clearAnimation();
                menuItem.setActionView(null);
            }
        }
    }

    /**
     * item做动画
     *
     * @param item
     */
    private void showAnimate(MenuItem item) {
        hideAnimate();
        menuItem = item;
        //这里使用一个ImageView设置成MenuItem的ActionView，这样我们就可以使用这个ImageView显示旋转动画了
        ImageView qrView = (ImageView) getLayoutInflater().inflate(R.layout.action_view, null);
        qrView.setImageResource(R.drawable.sync);
        menuItem.setActionView(qrView);
        //显示动画
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.shake);
        animation.setRepeatMode(Animation.RESTART);
        animation.setRepeatCount(Animation.INFINITE);
        qrView.startAnimation(animation);
    }
}
