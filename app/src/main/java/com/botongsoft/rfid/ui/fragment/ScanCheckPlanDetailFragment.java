package com.botongsoft.rfid.ui.fragment;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.botongsoft.rfid.R;
import com.botongsoft.rfid.bean.classity.Kf;
import com.botongsoft.rfid.bean.classity.Mjj;
import com.botongsoft.rfid.bean.classity.Mjjg;
import com.botongsoft.rfid.common.db.DBDataUtils;
import com.botongsoft.rfid.common.db.DataBaseCreator;
import com.botongsoft.rfid.listener.OnItemClickListener;
import com.botongsoft.rfid.ui.activity.BaseActivity;
import com.botongsoft.rfid.ui.activity.CheckPlanDetailActivity;
import com.botongsoft.rfid.ui.adapter.ScanCheckPlanDetailAdapter;
import com.botongsoft.rfid.ui.widget.RecyclerViewDecoration.ListViewDescDecoration;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.exception.DbException;
import com.yanzhenjie.recyclerview.swipe.Closeable;
import com.yanzhenjie.recyclerview.swipe.OnSwipeMenuItemClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenu;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;

/**
 * Created by pc on 2017/6/26.
 */

public class ScanCheckPlanDetailFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final int UI_SUCCESS = 0;
    private static final int UI_NOMJG_ERROR = 1;
    private static final int UI_NOSCANFW_ERROR = 2;
    private static final int MSG_UPDATE_INFO = 1;
    @BindView(R.id.recycler_view)
    SwipeMenuRecyclerView mSwipeMenuRecyclerView;
    @BindView(R.id.tx_layout)
    TextInputLayout mTextInputLayout;
    @BindView(R.id.input_tx)
    TextInputEditText mTextInputEditText;
    @BindView(R.id.tv_info)
    TextView mTextView;
    CheckPlanDetailActivity parentActivity;
    private static String scanInfoLocal = "";//扫描的格子位置 根据“/”拆分后存入数据库
    private static String scanInfoNow = "";//扫描的格子位置
    private String editString;
    private List<Map> mDataLists = new ArrayList<>();
    private List<Mjj> mjjLists = new ArrayList<>();
    private List mjjgList = new ArrayList();
    private String[] srrArray;
    private Map mjjgMap = new HashMap();//用来存放扫描过的密集格
    private ScanCheckPlanDetailAdapter scanCheckPlanDetailAdapter;
    private ScanCheckPlanDetailFragment mContext;
    private HandlerThread mCheckMsgThread;//Handler线程池
    //后台运行的handler
    private Handler mCheckMsgHandler;
    //与UI线程管理的handler
    private Handler mHandler;
    private boolean isOnScreen;//是否在屏幕上
    private boolean isRun;//是否在RFID读取

    //传递后台运行消息队列
    Message msg;
    //传递UI前台显示消息队列
    Message mHandlerMessage;
    Bundle mBundle;
    private int type;
    private int pdid;
    private String fw;
    private static int size1 = 1;

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

    }

    @Override
    public void onCreate(Bundle savedInstanceState) { //此处可以设置Dialog的style等等
        super.onCreate(savedInstanceState);
        //        setCancelable(false);//无法直接点击外部取消dialog
        //        setStyle(DialogFragment.STYLE_NO_FRAME,0); //NO_FRAME就是dialog无边框，0指的是默认系统Theme
        parentActivity = (CheckPlanDetailActivity) getActivity();
        initUiHandler();

    }

    @Override
    protected void initData(boolean isSavedNull) {
        LinearLayoutManager layout = new LinearLayoutManager(getContext());
        mSwipeMenuRecyclerView.setLayoutManager(layout);// 布局管理器。
        layout.setStackFromEnd(true);//列表再底部开始展示，反转后由上面开始展示
        layout.setReverseLayout(true);//列表翻转
        mSwipeMenuRecyclerView.setHasFixedSize(true);// 如果Item够简单，高度是确定的，打开FixSize将提高性能。
        //        mSwipeMenuRecyclerView.setItemAnimator(new DefaultItemAnimator());// 设置Item默认动画，加也行，不加也行。
        mSwipeMenuRecyclerView.addItemDecoration(new ListViewDescDecoration());// 添加分割线。

        mTextInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 输入前的监听
                //                Log.e("输入前确认执行该方法", "开始输入");
                mCheckMsgHandler.removeMessages(MSG_UPDATE_INFO);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 输入的内容变化的监听
                //               Log.e("输入过程中执行该方法", "文字变化");
                if (mCheckMsgHandler != null) {
                    mCheckMsgHandler.removeCallbacks(delayRun);
                }
                mCheckMsgHandler.removeMessages(MSG_UPDATE_INFO);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // 输入后的监听
                //                Log.e("输入结束执行该方法", "输入结束");
                Log.e("Handler textChanged--->", String.valueOf(Thread.currentThread().getName()));
                if (mTextInputEditText.length() != 0) {
                    if (mCheckMsgHandler != null) {
                        mCheckMsgHandler.removeCallbacks(delayRun);
                    }
                    //延迟800ms，如果不再输入字符，则执行该线程的run方法 模拟扫描输入
                    msg = mCheckMsgHandler.obtainMessage();
                    msg.what = MSG_UPDATE_INFO;
                    mCheckMsgHandler.sendMessageDelayed(msg, 800);
                }
            }
        });
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
                        mTextInputEditText.setText("");
                        smoothMoveToPosition(mSwipeMenuRecyclerView, mDataLists.size() + 1);
                        scanCheckPlanDetailAdapter.notifyDataSetChanged();
                        break;
                    case UI_NOMJG_ERROR:
                        mTextInputEditText.setText("");
                        Toast.makeText(getContext(), "请先扫描密集格后再进行操作", Toast.LENGTH_SHORT).show();
                        break;
                    case UI_NOSCANFW_ERROR:
                        mTextInputEditText.setText("");
                        Toast.makeText(getContext(), "该密集格不在盘点范围内", Toast.LENGTH_SHORT).show();
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
        isOnScreen = true;
        //        if(isOnScreen && isRun) {
        if (isOnScreen) {
            //开启新进程
            mCheckMsgThread = new HandlerThread("BackThread");// 创建一个BackHandlerThread对象，它是一个线程
            mCheckMsgThread.start();// 启动线程
            //创建后台线程
            initBackThread();
        }
    }

    private void initBackThread() {
        mCheckMsgHandler = new Handler(mCheckMsgThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                Log.e("Handler BackThread--->", String.valueOf(Thread.currentThread().getName()));
                switch (msg.what) {
                    case MSG_UPDATE_INFO:
                        checkForUpdate();//
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
            mHandlerMessage = mHandler.obtainMessage();
            mHandlerMessage.what = UI_SUCCESS;
            editString = mTextInputEditText.getText().toString();
            searchDB(editString);
            mHandler.sendMessage(mHandlerMessage);
        }
    };

    private void searchDB(String editString) {
        boolean tempStr = true;
        boolean temp = true;
        //防止扫描重复判断
        //        if (mDataLists.size() > 0) {
        //            for (Map map : mDataLists) {
        //                if (map.get("title").toString().equals(editString)) {
        //                    tempStr = false;
        //                    break;
        //                }
        //            }
        //        }

        //先判断扫描的是否是格子标签，再判断该格子是否在盘点范围内
        Mjjg mjjg = (Mjjg) DBDataUtils.getInfo(Mjjg.class, "id", editString);

        if (mjjg == null) {//扫描的不是密集格标签 ，通知用户
            mHandlerMessage.what = UI_NOMJG_ERROR;
        } else {////先判断是否在该批次的盘点范围内
            try {
                DbUtils db = DataBaseCreator.create();
                String sql = "select * from com_botongsoft_rfid_bean_classity_Mjjg where mjjid = (select id from (select * from com_botongsoft_rfid_bean_classity_Mjj";
                Integer kfid = Integer.valueOf(srrArray[0]);
                Integer mjjid = 0;
                Integer zy = 0;
                if (kfid != 0) {
                    sql += " where kfid=" + kfid;
                    mjjid = Integer.valueOf(srrArray[1]);
                    if (mjjid != 0) {
                        sql += " and id=" + mjjid;
                        zy = Integer.valueOf(srrArray[2]);
                    }
                }
                sql += ") as a where a.id=" + mjjg.getMjjid() + ") and id=" + editString;
                if (kfid != 0) {
                    if (mjjid != 0) {
                        if (zy != 0) {
                            sql += " and zy =" + zy;
                        }
                    }
                }
                Cursor cursor = (Cursor) db.execQuery(sql); // 执行自定义sql
                int s = cursor.getCount();
                if (s == 0) {
                    temp = false;
                    mHandlerMessage.what = UI_NOSCANFW_ERROR;
                }
                db.close();
            } catch (DbException e) {
                e.printStackTrace();
            }
            if (temp) {
                mjjgList.add(0, editString);
                if (mjjgList.size() >= 2) {//再判断上一次是否是扫描过的格子，防止重复读取数据库;
                    if (mjjgList.get(0).equals(mjjgList.get(1))) {//当前读取的跟上一次读取的id相同
                        mjjgList.remove(0);
                    } else {
                        //读取密集格内档案数据显示在列表上
                        dislapView(mjjg);
                    }
                } else if (mjjgList.size() <= 1) {//list为空或小于1 代表第一次读取
                    //读取密集格内档案数据显示在列表上
                    dislapView(mjjg);
                }
            }
        }
        //模拟数据
        //            Map map = new HashMap();
        //            map.put("id", size1++);
        //            map.put("title", mTextInputEditText.getText());
        //            map.put("local", "1库2架左2组2层" + size1);
        //            mDataLists.add(map);


    }

    private void dislapView(Mjjg mjjg) {
        String kfname = "";
        String mjjname = "";
        String nLOrR = "";
        Kf kf = null;
        Mjj mjj = (Mjj) DBDataUtils.getInfo(Mjj.class, "id", mjjg.getMjjid() + "");
        if (mjj != null) {
            mjjname = mjj.getMc() + "/";
            kf = (Kf) DBDataUtils.getInfo(Kf.class, "id", mjj.getKfid() + "");
        }
        if (kf != null) {
            kfname = kf.getMc() + "/";
        }
        nLOrR = mjjg.getZy() == 1 ? "左" : "右";
        String name = kfname + mjjname + nLOrR + "/" + mjjg.getZs() + "组" + mjjg.getCs() + "层";
        String temple = kf.getId() + "/" + mjj.getId() + "/" + mjjg.getId();//这里的值用来拆分存放位置存入档案表
        mBundle = new Bundle();
        mBundle.putString("info", name);
        scanInfoLocal = temple;
        mHandlerMessage.setData(mBundle);
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
            sb.append("Title:").append(mDataLists.get(position).get("title")).append("\n");
            sb.append("位置:").append(mDataLists.get(position).get("local")).append("\n");
            new AlertDialog.Builder(BaseActivity.activity)
                    .setTitle("详细信息：")
                    .setMessage(sb)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {

                        }
                    })
                    .create().show();
        }

        @Override
        public void onItemClick(int position, int listSize) {
            Toast.makeText(getContext(), "我是第" + position + "条。", Toast.LENGTH_SHORT).show();

            if (position != -1) {
                mDataLists.remove(position);
                scanCheckPlanDetailAdapter.notifyItemRemoved(position);
                scanCheckPlanDetailAdapter.notifyItemRangeChanged(position, listSize);
            }
        }
    };
}
