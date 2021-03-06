package com.botongsoft.rfid.ui.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.botongsoft.rfid.BaseApplication;
import com.botongsoft.rfid.R;
import com.botongsoft.rfid.Receiver.KeyReceiver;
import com.botongsoft.rfid.bean.classity.CheckError;
import com.botongsoft.rfid.bean.classity.CheckPlanDeatil;
import com.botongsoft.rfid.bean.classity.CheckPlanDeatilDel;
import com.botongsoft.rfid.bean.classity.Epc;
import com.botongsoft.rfid.bean.classity.Kf;
import com.botongsoft.rfid.bean.classity.Mjj;
import com.botongsoft.rfid.bean.classity.Mjjg;
import com.botongsoft.rfid.bean.classity.Mjjgda;
import com.botongsoft.rfid.common.Constant;
import com.botongsoft.rfid.common.db.DBDataUtils;
import com.botongsoft.rfid.common.db.SearchDb;
import com.botongsoft.rfid.common.utils.ConverJavaBean;
import com.botongsoft.rfid.common.utils.LogUtils;
import com.botongsoft.rfid.common.utils.SNUtil;
import com.botongsoft.rfid.common.utils.SoundUtil;
import com.botongsoft.rfid.common.utils.ToastUtils;
import com.botongsoft.rfid.listener.OnItemClickListener;
import com.botongsoft.rfid.ui.adapter.ScanCheckPlanDetailAdapter;
import com.botongsoft.rfid.ui.widget.RecyclerViewDecoration.ListViewDescDecoration;
import com.handheld.UHFLonger.UHFLongerManager;
import com.yanzhenjie.recyclerview.swipe.Closeable;
import com.yanzhenjie.recyclerview.swipe.OnSwipeMenuItemClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenu;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

import static com.botongsoft.rfid.common.Constant.coverNum;
import static com.botongsoft.rfid.common.Constant.getMjjg;
import static com.botongsoft.rfid.common.db.DBDataUtils.getCount;
import static com.botongsoft.rfid.common.db.DBDataUtils.getInfo;
import static com.botongsoft.rfid.ui.activity.BaseActivity.activity;

/**
 * 出错这里要报警，RFID读取要终止。要不读取档案记录的话还会添加进去。
 * 出错返回activity 要有提示
 * Created by pc on 2017/6/26.
 */

public class ScanCheckPlanDetailFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final int UI_SUCCESS = 0;
    private static final int UI_NOMJG_ERROR = 1;
    private static final int UI_NOSCANFW_ERROR = 2;
    private static final int MSG_UPDATE_INFO = 1;
    private static final int UI_SAVE_ERROR = 3;
    @BindView(R.id.recycler_view)
    SwipeMenuRecyclerView mSwipeMenuRecyclerView;
    //    @BindView(R.id.tx_layout)
    //    TextInputLayout mTextInputLayout;
    //    @BindView(R.id.input_tx)
    //    TextInputEditText mTextInputEditText;
    @BindView(R.id.st_saoma)
    Switch mSwitch;
    @BindView(R.id.st_ajzt)
    Switch mSwitch_ajzt;
    @BindView(R.id.tv_info)
    TextView mTextView;
    //    CheckPlanDetailActivity parentActivity;
    private static String scanInfoLocal = "";//扫描的格子位置 根据“/”拆分后存入数据库
    //    private static String scanInfoNow = "";//扫描的格子位置
    private String editString;
    private List<Mjjgda> mDataLists = new ArrayList<Mjjgda>();
    private List<String> ajztList = new ArrayList<String>();
    private List<Mjj> mjjLists = new ArrayList<>();
    private List mjjgList = new ArrayList();
    private List<CheckPlanDeatilDel> delTempList = new ArrayList<>();
    private String[] srrArray;
    private List<String> stringList = new ArrayList<>();//用来存放扫描过的密集格档案 防止重复扫描
    private ScanCheckPlanDetailAdapter scanCheckPlanDetailAdapter;
    private ScanCheckPlanDetailFragment mContext;
    private HandlerThread mCheckMsgThread;//Handler线程池
    //后台运行的handler
    private Handler mCheckMsgHandler;
    //与UI线程管理的handler
    private Handler mHandler;
    private boolean isOnScreen;//是否在屏幕上
    private boolean isRun;//是否在RFID读取
    private boolean ajztFlag;

    //传递后台运行消息队列
    Message msg;
    //传递UI前台显示消息队列
    Message mHandlerMessage;
    Bundle mBundle;
    Bundle saveErrBundele;
    private int type;
    private int pdid;
    private String fw;
    //    private static int size1 = 1;
    private static int oldMjjId;
    private static int oldMjgId;
    private static int oldKfId;
    private static int oldZy;
    //    private SoundPool soundPool;
    //    private PlaySoundPool soundPool;
    Thread thread;
    private boolean runFlag = true;
    private boolean startFlag = false;
    private KeyReceiver keyReceiver;
    private static UHFLongerManager manager;

    public static ScanCheckPlanDetailFragment newInstance(int type, int pdid, String fw) {
        Bundle args = new Bundle();
        args.putInt("type", type);
        args.putInt("pdid", pdid);
        args.putString("fw", fw);
        ScanCheckPlanDetailFragment fragment = new ScanCheckPlanDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onRefresh() {

    }

    @Override
    protected void initRootView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.scancheckplandetail_fragment, container, false);
        if (getArguments() != null) {
            type = getArguments().getInt("type");
            pdid = getArguments().getInt("pdid");
            fw = getArguments().getString("fw");
            srrArray = fw.split(",");
        }
    }


    @Override
    protected void initEvents() {
        try {
            manager = BaseApplication.application.getmanager();
            SharedPreferences sp = BaseApplication.application.getSharedPreferences("power", 0);
            //            int value = ShareManager.getInt(this, "power");
            int value = sp.getInt("value", 0);
            if (value == 0) {
                value = 30;
            }
            manager.setOutPower((short) value);

        } catch (Exception e) {
            e.printStackTrace();
        }
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // 开启switch，设置提示信息
                    startFlag = true;
                } else {
                    // 关闭swtich，设置提示信息
                    startFlag = false;
                }
            }
        });
        mSwitch_ajzt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // 开启switch，设置提示信息
                    ajztFlag = true;
                    switchChange();
                } else {
                    // 关闭swtich，设置提示信息
                    ajztFlag = false;
                    switchChange();
                }
            }
        });
        thread = new ThreadMe();
        thread.start();
        keyReceiver = new KeyReceiver(manager, false, mSwitch);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.rfid.FUN_KEY");
        activity.registerReceiver(keyReceiver, intentFilter);
    }

    private void switchChange() {
        if (mDataLists != null && mDataLists.size() > 0) {
            mDataLists.clear();
        }
        if (ajztList != null && ajztList.size() > 0) {
            ajztList.clear();
        }
        if (mjjgList != null && mjjgList.size() > 0) {
            mjjgList.remove(0);
        }
        scanCheckPlanDetailAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) { //此处可以设置Dialog的style等等
        super.onCreate(savedInstanceState);
        //        setCancelable(false);//无法直接点击外部取消dialog
        //        setStyle(DialogFragment.STYLE_NO_FRAME,0); //NO_FRAME就是dialog无边框，0指的是默认系统Theme
        //        parentActivity = (CheckPlanDetailActivity) getActivity();//目测没用 先注释掉
        initUiHandler();

    }

    @Override
    protected void initData(boolean isSavedNull) {
        LinearLayoutManager layout = new LinearLayoutManager(getContext());
        mSwipeMenuRecyclerView.setLayoutManager(layout);// 布局管理器。
        //        layout.setStackFromEnd(true);//列表再底部开始展示，反转后由上面开始展示
        //        layout.setReverseLayout(true);//列表翻转
        mSwipeMenuRecyclerView.setHasFixedSize(true);// 如果Item够简单，高度是确定的，打开FixSize将提高性能。
        //        mSwipeMenuRecyclerView.setItemAnimator(new DefaultItemAnimator());// 设置Item默认动画，加也行，不加也行。
        mSwipeMenuRecyclerView.addItemDecoration(new ListViewDescDecoration());// 添加分割线。

        //        mTextInputEditText.addTextChangedListener(new TextWatcher() {
        //            @Override
        //            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        //                // 输入前的监听
        //                //                Log.e("输入前确认执行该方法", "开始输入");
        //                mCheckMsgHandler.removeMessages(MSG_UPDATE_INFO);
        //            }
        //
        //            @Override
        //            public void onTextChanged(CharSequence s, int start, int before, int count) {
        //                // 输入的内容变化的监听
        //                //               Log.e("输入过程中执行该方法", "文字变化");
        //                if (mCheckMsgHandler != null) {
        //                    mCheckMsgHandler.removeCallbacks(delayRun);
        //                }
        //                mCheckMsgHandler.removeMessages(MSG_UPDATE_INFO);
        //            }
        //
        //            @Override
        //            public void afterTextChanged(Editable editable) {
        //                // 输入后的监听
        //                //                Log.e("输入结束执行该方法", "输入结束");
        //                Log.e("Handler textChanged--->", String.valueOf(Thread.currentThread().getName()));
        //                if (mTextInputEditText.length() != 0) {
        //                    if (mCheckMsgHandler != null) {
        //                        mCheckMsgHandler.removeCallbacks(delayRun);
        //                    }
        //                    //延迟800ms，如果不再输入字符，则执行该线程的run方法 模拟扫描输入
        //                    msg = mCheckMsgHandler.obtainMessage();
        //                    msg.what = MSG_UPDATE_INFO;
        //                    mCheckMsgHandler.sendMessageDelayed(msg, Constant.delayRun);
        //                }
        //            }
        //        });
        // 添加滚动监听。
        //        mSwipeMenuRecyclerView.addOnScrollListener(mOnScrollListener);
        // 设置菜单创建器。
        mSwipeMenuRecyclerView.setSwipeMenuCreator(swipeMenuCreator);
        // 设置菜单Item点击监听。
        mSwipeMenuRecyclerView.setSwipeMenuItemClickListener(menuItemClickListener);

        scanCheckPlanDetailAdapter = new ScanCheckPlanDetailAdapter(getActivity(), mDataLists);
        scanCheckPlanDetailAdapter.setOnItemClickListener(onItemClickListener);
        mSwipeMenuRecyclerView.setAdapter(scanCheckPlanDetailAdapter);

    }

    private void initUiHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case UI_SUCCESS:
                        if (mBundle != null) {
                            mTextView.setText(mBundle.getString("info"));
                        }
                        //                        mTextInputEditText.setText("");
                        //                        smoothMoveToPosition(mSwipeMenuRecyclerView, mDataLists.size() + 1);

                        if (msg.obj != null) {
                            int position = (int) msg.obj;
                            scanCheckPlanDetailAdapter.notifyItemChanged(position);
                        } else {
                            scanCheckPlanDetailAdapter.notifyDataSetChanged();
                        }
                        //                        scanCheckPlanDetailAdapter.notifyDataSetChanged();
                        break;
                    case UI_NOMJG_ERROR:
                        //                        mTextInputEditText.setText("");
                        Toast.makeText(getContext(), "请先扫描密集格后再进行操作", Toast.LENGTH_SHORT).show();
                        break;
                    case UI_NOSCANFW_ERROR:
                        //                        mTextInputEditText.setText("");
                        Toast.makeText(getContext(), "该密集格不在盘点范围内", Toast.LENGTH_SHORT).show();
                        break;
                    case UI_SAVE_ERROR:
                        if (saveErrBundele != null) {
                            //                            mTextInputEditText.setText("");
                            String bm = (saveErrBundele.getString("bm"));
                            String jlid = (saveErrBundele.getString("jlid"));
                            String mjg = (saveErrBundele.getString("mjg"));
                            Toast.makeText(getContext(), "记录保存失败," + "错误记录编码 " + bm + "-" + jlid, Toast.LENGTH_SHORT).show();
                        }

                        break;
                    default:
                        super.handleMessage(msg);//这里最好对不需要或者不关心的消息抛给父类，避免丢失消息
                        break;
                }
            }
        };
    }

    /*
     * 目标项是否在最后一个可见项之后
     */
    boolean mShouldScroll;
    /**
     * 记录目标项位置
     */
    int mToPosition;

    /**
     * 滑动到指定位置
     *
     * @param mRecyclerView
     * @param position
     */
    public void smoothMoveToPosition(RecyclerView mRecyclerView, final int position) {
        // 第一个可见位置
        int firstItem = mRecyclerView.getChildLayoutPosition(mRecyclerView.getChildAt(0));
        // 最后一个可见位置
        int lastItem = mRecyclerView.getChildLayoutPosition(mRecyclerView.getChildAt(mRecyclerView.getChildCount() - 1));

        if (position < firstItem) {
            // 如果跳转位置在第一个可见位置之前，就smoothScrollToPosition可以直接跳转
            mRecyclerView.smoothScrollToPosition(position);
        } else if (position <= lastItem) {
            // 跳转位置在第一个可见项之后，最后一个可见项之前
            // smoothScrollToPosition根本不会动，此时调用smoothScrollBy来滑动到指定位置
            int movePosition = position - firstItem;
            if (movePosition >= 0 && movePosition < mRecyclerView.getChildCount()) {
                int top = mRecyclerView.getChildAt(movePosition).getTop();
                mRecyclerView.smoothScrollBy(0, top);
            }
        } else {
            // 如果要跳转的位置在最后可见项之后，则先调用smoothScrollToPosition将要跳转的位置滚动到可见位置
            // 再通过onScrollStateChanged控制再次调用smoothMoveToPosition，执行上一个判断中的方法
            mRecyclerView.smoothScrollToPosition(position);
            mToPosition = position;
            mShouldScroll = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("onResume--->", String.valueOf(Thread.currentThread().getName()));
        isOnScreen = true;
        //        if(isOnScreen && isRun) {
        if (isOnScreen) {
            //开启新进程
            //            mCheckMsgThread = new HandlerThread("BackThread");// 创建一个BackHandlerThread对象，它是一个线程
            //            mCheckMsgThread.start();// 启动线程
            //创建后台线程
            //            initBackThread();
        }
        mSwitch.setChecked(false);
        mSwitch_ajzt.setChecked(false);
    }

    private void initBackThread() {
        mCheckMsgHandler = new Handler(mCheckMsgThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                Log.e("Handler BackThread--->", String.valueOf(Thread.currentThread().getName()));
                switch (msg.what) {
                    case MSG_UPDATE_INFO:
                        //                        checkForUpdate();//
                        break;
                    default:
                        super.handleMessage(msg);//这里最好对不需要或者不关心的消息抛给父类，避免丢失消息
                        break;
                }
            }
        };
    }


    /**
     * 延迟线程，看是否还有下一个字符输入
     */

    private void checkForUpdate() {
        mCheckMsgHandler.post(delayRun);
    }

    private Runnable delayRun = new Runnable() {

        @Override
        public void run() {
            //在这里调用服务器的接口，获取数据
            Log.e("Handler delayRun--->", String.valueOf(Thread.currentThread().getName()));
            //            mHandler.obtainMessage(UI_SUCCESS).sendToTarget();
            //            mTextView.post(new Runnable() {
            //                @Override
            //                public void run() {
            //                    Log.e("setText--->", String.valueOf(Thread.currentThread().getName()));
            //                    mTextView.setText(mTextInputEditText.getText());
            //                    mTextInputEditText.setText("");
            //                }
            //            });
            //在这里读取数据库增加list值，界面显示读取的标签信息
            //这里定义发送通知ui更新界面
            //            mHandlerMessage = mHandler.obtainMessage();
            //            mHandlerMessage.what = UI_SUCCESS;
            //            editString = mTextInputEditText.getText().toString();
            //            searchDB(editString);
            //            mHandler.sendMessage(mHandlerMessage);
        }
    };
    private static int text = 0;
    private static int nowMjjgId = 0;
    private static int nowMjjId = 0;
    private static int nowKfId = 0;
    private static int nowMjjgZy = 0;

    private void searchDB(String editString) {
        boolean tempStr = true;//防止重复扫描状态标记
        boolean temp = true;//是否在盘点范围标记
        boolean ajztTempFlag = true;
        mHandlerMessage = mHandler.obtainMessage();
        mHandler.sendMessage(mHandlerMessage);
        int lx = Constant.getLx(editString);//根据传入的值返回对象类型
        switch (lx) {
            case Constant.LX_MJJG:
                displayMjg(editString, temp);
                break;
        }

        switch (lx) {
            case Constant.LX_AJZT:

                if (ajztTempFlag && ajztFlag) {
                    if (scanInfoLocal.equals("")) {
                        mHandlerMessage.what = UI_NOMJG_ERROR;
                    } else {
                        tempStr = newfcf(editString, tempStr, ajztTempFlag, lx);//新防重复（有案卷载体开关）
                        if (tempStr) {
                            int code = Integer.parseInt(editString.substring(1, editString.length()));
                            List<Epc> epcList = (List<Epc>) DBDataUtils.getInfos(Epc.class, "ztcode", String.valueOf(code));
                            if (epcList != null && epcList.size() > 0) {
                                for (Epc epc1 : epcList) {
                                    String epcs = String.valueOf(epc1.getEpccode());
                                    epcs = coverNum(epcs);
                                    for (int i = mDataLists.size() - 1; i >= 0; i--) {
                                        Mjjgda mjjgda = mDataLists.get(i);
                                        String epcCode = coverNum(mjjgda.getEpccode());
                                        //                            if ((mjjgda.getBm().equals(ecp.getBm())) && (String.valueOf(mjjgda.getJlid()).equals(ecp.getJlid()))) {
                                        if (epcCode.equals(epcs)) {
                                            if (mjjgda.getFlag() == 1) {
                                                mjjgda.setColor(3);//外借被找到
                                            } else {
                                                mjjgda.setColor(2);//正确
                                            }
                                            mHandlerMessage.obj = i;
                                            text = 1;
                                            break;
                                        }
                                        if (i == 0) {//如果循环完毕 都没有在这个格子中找到扫描的这个档案，变量设成0，然后在下面加入mDataLists中显示出新的条目数据
                                            text = 0;
                                        }
                                    }
                                    if (text == 0) {

                                        Mjjgda mjjgda = new Mjjgda();
                                        mjjgda.setBm(epc1.getBm());
                                        mjjgda.setJlid(String.valueOf(epc1.getJlid()));
                                        mjjgda.setColor(4);//多扫描或错架
                                        mjjgda.setMjjid(nowMjjId);
                                        mjjgda.setKfid(nowKfId);
                                        mjjgda.setMjgid(nowMjjgId);
                                        mjjgda.setTitle(epc1.getArchiveno());
                                        mjjgda.setEpccode(String.valueOf(epc1.getEpccode()));
                                        mDataLists.add(mjjgda);
                                        SoundUtil.play(1, 0);
                                    }
                                }
                            }
                        }

                    }
                }

                break;
            case Constant.LX_MJGDA:
                if (scanInfoLocal.equals("")) {
                    mHandlerMessage.what = UI_NOMJG_ERROR;
                } else {
                    //防止扫描重复判断
                    //                    tempStr = oldfcf(editString, tempStr);//旧防重复（没有案卷载体开关）
                    if (!ajztFlag) {
                        tempStr = newfcf(editString, tempStr, ajztTempFlag, lx);//新防重复（有案卷载体开关）
                        if (tempStr) {
                            //这里要先查询一次对照表 获得该扫描记录的bm与jlid
                            //                        Epc ecp = (Epc) DBDataUtils.getInfo(Epc.class, "epccode", editString);
                            for (int i = mDataLists.size() - 1; i >= 0; i--) {
                                Mjjgda mjjgda = mDataLists.get(i);
                                String epcCode = coverNum(mjjgda.getEpccode());
                                //                            if ((mjjgda.getBm().equals(ecp.getBm())) && (String.valueOf(mjjgda.getJlid()).equals(ecp.getJlid()))) {
                                if (epcCode.equals(editString)) {
                                    if (mjjgda.getFlag() == 1) {
                                        mjjgda.setColor(3);//外借被找到
                                    } else {
                                        mjjgda.setColor(2);//正确
                                    }
                                    mHandlerMessage.obj = i;
                                    text = 1;
                                    break;
                                }
                                if (i == 0) {//如果循环完毕 都没有在这个格子中找到扫描的这个档案，变量设成0，然后在下面加入mDataLists中显示出新的条目数据
                                    text = 0;
                                }
                            }
                            if (text == 0) {
                                Epc ecp = (Epc) DBDataUtils.getInfo(Epc.class, "epccode", editString);
                                LogUtils.d("newErrorData", ecp.getBm() + "-" + ecp.getJlid());
                                Mjjgda mjjgda = new Mjjgda();
                                mjjgda.setBm(ecp.getBm());
                                mjjgda.setJlid(String.valueOf(ecp.getJlid()));
                                mjjgda.setColor(4);//多扫描或错架
                                mjjgda.setMjjid(nowMjjId);
                                mjjgda.setKfid(nowKfId);
                                mjjgda.setMjgid(nowMjjgId);
                                mjjgda.setTitle(ecp.getArchiveno());
                                mjjgda.setEpccode(String.valueOf(ecp.getEpccode()));
                                mDataLists.add(mjjgda);
                                SoundUtil.play(1, 0);
                            }
                            stringList.add(editString);
                        }
                    }


                }
                break;
            case Constant.LX_MJJG:
                //                displayMjg(editString, temp);
                break;
        }
    }

    private void displayMjg(String editString, boolean temp) {
        String s[] = Constant.reqDatas(editString);
        //如果不重复查询密集格表
        //        Mjjg mjjg = (Mjjg) DBDataUtils.getInfo(Mjjg.class, "mjjid", Integer.valueOf(s[2]).toString(), "zy", Integer.valueOf(s[3]).toString(),
        //                "cs", Integer.valueOf(s[5]).toString(), "zs", Integer.valueOf(s[4]).toString());
        Mjjg mjjg = getMjjg(s);
        if (mjjg != null) {
            nowMjjgId = mjjg.getId();
            nowMjjgZy = mjjg.getZy();
            int tt = SearchDb.countPdfw(srrArray, mjjg);//先判断是否在该批次的盘点范围内
            if (tt == 0) {
                temp = false;//false为不在盘点范围
                mHandlerMessage.what = UI_NOSCANFW_ERROR;
            }
            if (temp) {//temp=true 在盘点范围内
                Mjj mjj = (Mjj) getInfo(Mjj.class, "id", String.valueOf(mjjg.getMjjid()));
                nowMjjId = mjj.getId();
                nowKfId = mjj.getKfid();
                mjjgList.add(0, editString);
                if (mjjgList.size() >= 2) {//再判断上一次是否是扫描过的格子，防止重复读取数据库;
                    if (mjjgList.get(0).equals(mjjgList.get(1))) {//当前读取的跟上一次读取的id相同
                        mjjgList.remove(0);
                    } else {
                        //判断当前格子是否被扫描过(同批次)，有的话清除已扫描的错误记录表
                        clearOrSaveCheckError(mjjg, mjj);
                        //保存错误记录
                        boolean save = savePdjl(mDataLists, pdid);
                        //保存成功 读取新密集格内档案数据显示在列表上
                        if (save == true) {
                            dislapView(mjjg);
                        } else {
                            mjjgList.remove(0);
                        }

                    }
                } else if (mjjgList.size() <= 1) {//list为空或小于1 代表第一次读取
                    //判断当前格子是否被扫描过(同批次)，有的话清除已扫描的错误记录表
                    clearOrSaveCheckError(mjjg, mjj);
                    //读取密集格内档案数据显示在列表上
                    dislapView(mjjg);
                }
            }
        }
    }

    /*/
    新防重复
     */
    private boolean newfcf(String editString, boolean tempStr, boolean ajztTempFlag, int lx) {
        if (ajztFlag) {//
            tempStr = false;
            if (lx == Constant.LX_AJZT) {
                if (ajztList != null && ajztList.size() == 0) {
                    ajztTempFlag = true;
                    tempStr = true;
                    ajztList.add(editString);
                } else {
                    for (String s : ajztList) {
                        if (s.equals(editString)) {
                            ajztTempFlag = false;
                            tempStr = false;
                            break;
                        } else {
                            tempStr = true;
                        }
                    }
                    if (ajztTempFlag) {
                        ajztList.add(editString);
                    }
                }
            }
        } else {
            //防止扫描重复判断
            if (lx != Constant.LX_AJZT) {
                tempStr = oldfcf(editString, tempStr);
            }
        }
        return tempStr;
    }

    private boolean oldfcf(String editString, boolean tempStr) {
        if (stringList.size() > 0) {
            for (String s : stringList) {
                if (s.equals(editString)) {
                    tempStr = false;
                    break;
                }
            }
        }
        return tempStr;
    }

    private void clearOrSaveCheckError(Mjjg mjjg, Mjj mjj) {
        //        判断当前格子是否被扫描过(同批次)，有的话清除已扫描的错误记录表
        CheckError ce = (CheckError) DBDataUtils.getInfo(CheckError.class,
                "pdid", String.valueOf(pdid),
                "mjgid", String.valueOf(mjjg.getId()),
                "zy", String.valueOf(mjjg.getZy()),
                "mjjid", String.valueOf(mjjg.getMjjid()),
                "kfid", String.valueOf(mjj.getKfid()));
        if (ce != null) {//数据库有扫描过该格子了
            List<CheckPlanDeatil> mCheckPlanDeatilList = (List) DBDataUtils.getInfosHasOp(CheckPlanDeatil.class, "pdid", "=",
                    String.valueOf(pdid), "mjgid", "=", String.valueOf(mjjg.getId()));
            if (mCheckPlanDeatilList != null && !mCheckPlanDeatilList.isEmpty()) {
                for (CheckPlanDeatil checkPlanDeatil : mCheckPlanDeatilList) {
                    CheckPlanDeatilDel cd = ConverJavaBean.toAnotherObj(checkPlanDeatil, CheckPlanDeatilDel.class);
                    delTempList.add(cd);
                }
                DBDataUtils.saveAll(delTempList);
            }
            //清楚数据库扫过的该批次的格子内容
            DBDataUtils.deleteInfos(CheckPlanDeatil.class, "pdid", String.valueOf(pdid), "mjgid", String.valueOf(mjjg.getId()));
            delTempList.clear();
            ce.setAnchor(0);//将扫描过的版本号设置为0，提交服务器的时候 ，服务器会删除该格子的旧数据。
            DBDataUtils.update(ce);
        } else {
            CheckError mCheckError = new CheckError();
            mCheckError.setMjgid(mjjg.getId());
            mCheckError.setPdid(pdid);
            mCheckError.setZy(mjjg.getZy());
            mCheckError.setMjjid(mjjg.getMjjid());
            mCheckError.setKfid(mjj.getKfid());
            mCheckError.setStatus(0);
            //            mCheckError.setAnchor(SNUtil.nextSid());
            mCheckError.setAnchor(0);
            DBDataUtils.save(mCheckError);
        }
    }

    private boolean savePdjl(List<Mjjgda> mDataLists, int pdid) {
        boolean save = true;
        for (Mjjgda mDataList : mDataLists) {
            if (mDataList.getColor() != 2) {
                CheckPlanDeatil mCheckPlanDeatil = null;
                if (save) {
                    try {
                        mCheckPlanDeatil = new CheckPlanDeatil();
                        mCheckPlanDeatil.setBm(mDataList.getBm());
                        mCheckPlanDeatil.setJlid(Integer.valueOf(mDataList.getJlid()));
                        mCheckPlanDeatil.setMjgid(mDataList.getMjgid());
                        mCheckPlanDeatil.setKfid(mDataList.getKfid());
                        mCheckPlanDeatil.setZy(oldZy);
                        mCheckPlanDeatil.setMjjid(oldMjjId);
                        mCheckPlanDeatil.setPdid(pdid);
                        mCheckPlanDeatil.setAnchor(SNUtil.nextSid());
                        mCheckPlanDeatil.setStatus(0);
                        if (mDataList.getColor() == 3) {
                            mCheckPlanDeatil.setType(3);
                        } else if (mDataList.getColor() == 0) {
                            mCheckPlanDeatil.setType(2);
                        } else if (mDataList.getColor() == 4) {
                            mCheckPlanDeatil.setType(4);
                        }
                        DBDataUtils.save(mCheckPlanDeatil);
                    } catch (Exception e) {
                        save = false;
                        //出错这里要报警，RFID读取要终止。要不读取档案记录的话还会添加进去。
                        SoundUtil.play(2, 0);
                        mHandlerMessage.what = UI_SAVE_ERROR;
                        saveErrBundele = new Bundle();
                        saveErrBundele.putString("mjg", String.valueOf(mDataList.getMjgid()));
                        saveErrBundele.putString("bm", mDataList.getBm());
                        saveErrBundele.putString("jlid", mDataList.getJlid());
                        mHandlerMessage.setData(saveErrBundele);
                        Log.e("记录保存失败", "Exception = " + e);
                        e.printStackTrace();
                    }
                }
            }
        }
        return save;
    }

    private void dislapView(Mjjg mjjg) {
        stringList.clear();
        mDataLists.clear();
        displayTextLocal(mjjg);//界面显示扫描的格子位置
        displayMjgdaLocal(mjjg);//显示该格子内的档案数据
    }

    private void displayMjgdaLocal(Mjjg mjjg) {
        List<Mjjgda> mjjgdaList = new ArrayList<>();
        mjjgdaList = (List<Mjjgda>) DBDataUtils.getInfos(Mjjgda.class, "mjgid", String.valueOf(mjjg.getId()));
        for (Mjjgda mjjgdaThemp : mjjgdaList) {
            Epc ecp = (Epc) DBDataUtils.getInfo(Epc.class, "bm", mjjgdaThemp.getBm(), "jlid", mjjgdaThemp.getJlid());
            if (ecp != null) {
                mjjgdaThemp.setEpccode(String.valueOf(ecp.getEpccode()));
                mjjgdaThemp.setTitle(ecp.getArchiveno());
            }
        }
        mDataLists.addAll(mjjgdaList);
    }

    /**
     * 界面显示当前扫描的格子位置
     *
     * @param mjjg
     */
    private void displayTextLocal(Mjjg mjjg) {
        StringBuilder sb = new StringBuilder();
        String kfname = "";
        String mjjname = "";
        String nLOrR = "";
        Kf kf = null;
        Mjj mjj = (Mjj) getInfo(Mjj.class, "id", String.valueOf(mjjg.getMjjid()));
        if (mjj != null) {
            mjjname = mjj.getMc() + "/";
            kf = (Kf) getInfo(Kf.class, "id", String.valueOf(mjj.getKfid()));
        }
        if (kf != null) {
            kfname = kf.getMc() + "/";
        }
        nLOrR = mjjg.getZy() == 1 ? "左" : "右";
        //        String name = kfname + mjjname + nLOrR + "/" + mjjg.getZs() + "组" + mjjg.getCs() + "层";
        int countemp = getCount(Mjjgda.class, "mjgid", "=", String.valueOf(mjjg.getId()));
        int outCountTemp = getCount(Mjjgda.class, "mjgid", "=", String.valueOf(mjjg.getId()), "flag", "=", "1");
        sb.append(kfname).append(mjjname).append(nLOrR).append("/").append(mjjg.getZs()).append("组").append(mjjg.getCs()).append("层").append("\n");
        sb.append("共").append(countemp).append("案卷,").append("外借").append(outCountTemp).append("卷");
        String temple = kf.getId() + "/" + mjj.getId() + "/" + mjjg.getId();//这里的值用来拆分存放位置存入档案表
        mBundle = new Bundle();
        mBundle.putString("info", sb.toString());
        scanInfoLocal = temple;
        oldMjjId = mjj.getId();
        oldMjgId = mjjg.getId();
        oldKfId = kf.getId();
        oldZy = mjjg.getZy();
        mHandlerMessage.setData(mBundle);
    }

    @Override
    public void onDestroyView() {
        savePdjl(mDataLists, pdid);
        stringList.clear();
        scanInfoLocal = "";
        startFlag = false;
        runFlag = false;
        getActivity().unregisterReceiver(keyReceiver);
        thread.interrupt();
        super.onDestroyView();

    }

    /**
     * 菜单创建器。在Item要创建菜单的时候调用。
     */
    private SwipeMenuCreator swipeMenuCreator = new SwipeMenuCreator() {
        @Override
        public void onCreateMenu(SwipeMenu swipeLeftMenu, SwipeMenu swipeRightMenu, int viewType) {
            int width = getResources().getDimensionPixelSize(R.dimen.item_height);

            // MATCH_PARENT 自适应高度，保持和内容一样高；也可以指定菜单具体高度，也可以用WRAP_CONTENT。
            int height = ViewGroup.LayoutParams.MATCH_PARENT;

            // 添加左侧的，如果不添加，则左侧不会出现菜单。
           /* {
                SwipeMenuItem addItem = new SwipeMenuItem(mContext)
                        .setBackgroundDrawable(R.drawable.selector_green)// 点击的背景。
                        .setImage(R.mipmap.ic_action_add) // 图标。
                        .setWidth(width) // 宽度。
                        .setHeight(height); // 高度。
                swipeLeftMenu.addMenuItem(addItem); // 添加一个按钮到左侧菜单。

                SwipeMenuItem closeItem = new SwipeMenuItem(mContext)
                        .setBackgroundDrawable(R.drawable.selector_red)
                        .setImage(R.mipmap.ic_action_close)
                        .setWidth(width)
                        .setHeight(height);

                swipeLeftMenu.addMenuItem(closeItem); // 添加一个按钮到左侧菜单。
            }*/

            // 添加右侧的，如果不添加，则右侧不会出现菜单。
           /* {
                SwipeMenuItem deleteItem = new SwipeMenuItem(mContext)
                        .setBackgroundDrawable(R.drawable.selector_red)
                        .setImage(R.mipmap.ic_action_delete)
                        .setText("删除") // 文字，还可以设置文字颜色，大小等。。
                        .setTextColor(Color.WHITE)
                        .setWidth(width)
                        .setHeight(height);
                swipeRightMenu.addMenuItem(deleteItem);// 添加一个按钮到右侧侧菜单。

                SwipeMenuItem closeItem = new SwipeMenuItem(mContext)
                        .setBackgroundDrawable(R.drawable.selector_purple)
                        .setImage(R.mipmap.ic_action_close)
                        .setWidth(width)
                        .setHeight(height);
                swipeRightMenu.addMenuItem(closeItem); // 添加一个按钮到右侧菜单。

                SwipeMenuItem addItem = new SwipeMenuItem(mContext)
                        .setBackgroundDrawable(R.drawable.selector_green)
                        .setText("添加")
                        .setTextColor(Color.WHITE)
                        .setWidth(width)
                        .setHeight(height);
                swipeRightMenu.addMenuItem(addItem); // 添加一个按钮到右侧菜单。
            }*/
        }
    };
    /**
     * 菜单点击监听。
     */
    private OnSwipeMenuItemClickListener menuItemClickListener = new OnSwipeMenuItemClickListener() {
        /**
         * Item的菜单被点击的时候调用。
         * @param closeable       closeable. 用来关闭菜单。
         * @param adapterPosition adapterPosition. 这个菜单所在的item在Adapter中position。
         * @param menuPosition    menuPosition. 这个菜单的position。比如你为某个Item创建了2个MenuItem，那么这个position可能是是 0、1，
         * @param direction       如果是左侧菜单，值是：SwipeMenuRecyclerView#LEFT_DIRECTION，如果是右侧菜单，值是：SwipeMenuRecyclerView
         *                        #RIGHT_DIRECTION.
         */
        @Override
        public void onItemClick(Closeable closeable, int adapterPosition, int menuPosition, int direction) {
            closeable.smoothCloseMenu();// 关闭被点击的菜单。

            if (direction == SwipeMenuRecyclerView.RIGHT_DIRECTION) {
                Toast.makeText(getContext(), "list第" + adapterPosition + "; 右侧菜单第" + menuPosition, Toast.LENGTH_SHORT).show();
            } else if (direction == SwipeMenuRecyclerView.LEFT_DIRECTION) {
                Toast.makeText(getContext(), "list第" + adapterPosition + "; 左侧菜单第" + menuPosition, Toast.LENGTH_SHORT).show();
            }

            // TODO 推荐调用Adapter.notifyItemRemoved(position)，也可以Adapter.notifyDataSetChanged();
            if (menuPosition == 0) {// 删除按钮被点击。
                mDataLists.remove(adapterPosition);
                scanCheckPlanDetailAdapter.notifyItemRemoved(adapterPosition);
            }
        }
    };
    private OnItemClickListener onItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            //详细信息
            StringBuilder sb = new StringBuilder();
            //            sb.append("Title:").append(mDataLists.get(position).get("title")).append("\n");
            //            sb.append("位置:").append(mDataLists.get(position).get("local")).append("\n");
            //            new AlertDialog.Builder(BaseActivity.activity)
            //                    .setTitle("详细信息：")
            //                    .setMessage(sb)
            //                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
            //                        @Override
            //                        public void onDismiss(DialogInterface dialog) {
            //
            //                        }
            //                    })
            //                    .create().show();
        }

        @Override
        public void onItemClick(int position, int listSize) {
            // Toast.makeText(getContext(), "我是第" + position + "条。", Toast.LENGTH_SHORT).show();


            new AlertDialog.Builder(getContext())
                    .setMessage("确定要忽略这条错误吗？")
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (position != -1) {
                                mDataLists.get(position).setColor(2);
                                scanCheckPlanDetailAdapter.notifyItemChanged(position);
                                //                                mDataLists.remove(position);
                                //                                scanCheckPlanDetailAdapter.notifyItemRemoved(position);
                                //                                scanCheckPlanDetailAdapter.notifyItemRangeChanged(position, listSize);
                            }
                        }
                    })
                    .create().show();
        }

        @Override
        public void onItemClick(int position, int listSize, ProgressBar pb) {

        }
    };

    class ThreadMe extends Thread {
        private List<String> epcList;

        @Override
        public void run() {
            super.run();
            while (runFlag) {

                if (startFlag) {
                    if (BaseApplication.application.getmanager() != null) {
                        epcList = BaseApplication.application.getmanager().inventoryRealTime(); //
                        if (epcList != null && !epcList.isEmpty()) {
                            SoundUtil.play(1, 0);
                            Message sMessage = mHandler.obtainMessage();
                            sMessage.what = UI_SUCCESS;
                            for (String epc : epcList) {
                                searchDB(epc);

                            }
                            mHandler.sendMessage(sMessage);
                        }
                        epcList = null;
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } else {
                        runFlag = false;
                        startFlag = false;
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                ToastUtils.showLong("硬件链接失败");
                            }
                        });
                    }

                }
            }
        }

    }
}
