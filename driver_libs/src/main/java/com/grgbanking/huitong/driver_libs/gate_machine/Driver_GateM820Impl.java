package com.grgbanking.huitong.driver_libs.gate_machine;

import android.content.Context;

import com.grgbanking.huitong.driver_libs.bean.LeftPass;
import com.grgbanking.huitong.driver_libs.bean.LeftUnPass;
import com.grgbanking.huitong.driver_libs.bean.RightPass;
import com.grgbanking.huitong.driver_libs.bean.RightUnPass;
import com.grgbanking.huitong.driver_libs.database.DatabaseInstance;
import com.grgbanking.huitong.driver_libs.database.EntyType;
import com.grgbanking.huitong.driver_libs.interfaces.IDriver_GateMachine;
import com.grgbanking.huitong.driver_libs.interfaces.IGateMachineActionCallBack;
import com.grgbanking.huitong.driver_libs.util.SystemUtil;

import java.util.Date;


/**
 * Author: gongxiaobiao
 * Date: on 2019/9/10 15:52
 * Email: 904430803@qq.com
 * Description:  奥义机芯会自动关门，不需要手动调用方法，超时时间目前也是在机芯上设置默认五秒，
 * native层逻辑超时时间可自定义设置（即开门成功后一直未反馈过闸超时，此时会使用这个超时设置的时间）
 */
public class Driver_GateM820Impl extends IDriver_GateMachine {
    private int mHandle = -1;
    private GetPassByNumThread mGetPassByNumThread = null;
    volatile private PassTimeOutBean_M820 mPassTimeOutBean = new PassTimeOutBean_M820();//打开闸门指令时间

    @Deprecated
    @Override
    public int setMode(int p_hDevHandle, int mode, DevReturn devReturn) {
        return 0;

    }

    @Override
    public int setDriverLogDir(String logPath) {
        return IGateDev_M820.mInstance.setDriverLogDir(logPath);
    }

    @Override
    public int setConfigFileLoadDir(String configFilePath) {
        return IGateDev_M820.mInstance.SetConfigFileLoadDir(configFilePath);
    }

    @Override
    public int openLogicDevice(String p_pcLogicDevName) {
        return IGateDev_M820.mInstance.hOpenLogicDevice(p_pcLogicDevName);
    }

    @Override
    public int openLogicDevice(String p_pcLogicDevName, String configFilePath, String logFilePath) {
        System.out.println("TJZN初始化 openLogicDevice()");

        DevReturn devReturnsetPara = new DevReturn();
        DevReturn devReturnInit = new DevReturn();
        setConfigFileLoadDir(configFilePath
        );//设置配置文件路径
        setDriverLogDir(logFilePath
        );//设置闸机日志文件路径
        mHandle = IGateDev_M820.mInstance.hOpenLogicDevice(p_pcLogicDevName);
        setCommPara(mHandle, devReturnsetPara);
        init(mHandle, devReturnInit);
        if (devReturnsetPara.getiPhyCode() != 0 || devReturnsetPara.getiLogicCode() != 0) {
            System.err.println("TJZN初始化 setCommPara 失败" + "mHandle:" + mHandle + "devReturn" + devReturnsetPara.toString());
        } else {
            System.out.println("TJZN初始化 setCommPara()" + devReturnsetPara.toString());

        }
        if (devReturnInit.getiPhyCode() != 0 || devReturnInit.getiLogicCode() != 0) {
            System.err.println("TJZN初始化 init 失败" + "mHandle:" + mHandle + "devReturn" + devReturnInit.toString());
        } else {
            System.out.println("TJZN初始化 init()" + devReturnInit.toString());

        }
        mGetPassByNumThread = new GetPassByNumThread(getIGateMachineActionCallBack(), IGateDev_M820.mInstance, mHandle);
        mGetPassByNumThread.start();

        return mHandle;

    }

    @Override
    public void closeLogicDevice(int p_pcLogicDevName) {
        IGateDev_M820.mInstance.vCloseLogicDevice(p_pcLogicDevName);

    }

    @Override
    public int setCommPara(int devHandler, DevReturn devReturn) {
        MyDevReturn[] mydevReturn1 = creatMyDevReturnArray();
        return IGateDev_M820.mInstance.iSetCommPara(devHandler, mydevReturn1);
    }

    @Override
    public int init(int p_hDevHandle, DevReturn devReturn) {
        MyDevReturn[] mydevReturn = creatMyDevReturnArray();
        int ret = IGateDev_M820.mInstance.iInit(p_hDevHandle, mydevReturn);

        filldevReturn(devReturn, mydevReturn);
        return 0;
    }

    /**
     * @param
     * @return
     * @method
     * @description 设置超时时间，单位秒
     * @date: 2020/4/23 15:37
     * @author: gongxiaobiao
     */
    @Override
    public void setTimeout(int p_hDevHandle, int timeout) {
        MyDevReturn[] mydevReturn = creatMyDevReturnArray();
        IGateDev_M820.mInstance.iSetTimeout(p_hDevHandle, timeout * 1000, mydevReturn);

    }

    @Override
    public int openGateLeftOnce(DevReturn devReturn) {
        MyDevReturn[] mydevReturn1 = creatMyDevReturnArray();
//        int ret = IGateDev_M810.mInstance.iOpenGate(mHandle, 1, 0, mydevReturn1);
//        int ret = openGate(mHandle, 1, devReturn);
        mPassTimeOutBean.setLeftTimeOut(System.currentTimeMillis());
        mPassTimeOutBean.setLeftopened(true);
        mPassTimeOutBean.setOpenLeftTimes(1);
//        filldevReturn(devReturn, mydevReturn1);
//        getIGateMachineActionCallBack().openLeft(ret == 0);
        return 0;
    }

    /**
     * @param @times 开门次数
     * @return
     * @method
     * @description 描述一下方法的作用
     * @date: 2020/4/3 13:33
     * @author: gongxiaobiao
     */
    public void openGateLeft(DevReturn devReturn, int times) {
        MyDevReturn[] mydevReturn1 = creatMyDevReturnArray();
//        int ret = IGateDev_M810.mInstance.iOpenGate(mHandle, 1, 0, mydevReturn1);
//        int ret = openGate(mHandle, 1, devReturn);
        mPassTimeOutBean.setLeftTimeOut(System.currentTimeMillis());
        mPassTimeOutBean.setLeftopened(true);
        mPassTimeOutBean.setOpenLeftTimes(times <= 0 ? 1 : times);
//        filldevReturn(devReturn, mydevReturn1);
//        getIGateMachineActionCallBack().openLeft(ret == 0);
    }

    public void openGateRight(DevReturn devReturn, int times) {
        MyDevReturn[] mydevReturn1 = creatMyDevReturnArray();
//        int ret = IGateDev_M810.mInstance.iOpenGate(mHandle, 1, 0, mydevReturn1);
//        int ret = openGate(mHandle, 1, devReturn);
        mPassTimeOutBean.setRightTimeOut(System.currentTimeMillis());
        mPassTimeOutBean.setRightopened(true);
        mPassTimeOutBean.setOpenRightTimes(times <= 0 ? 1 : times);
//        filldevReturn(devReturn, mydevReturn1);
//        getIGateMachineActionCallBack().openLeft(ret == 0);
    }


    @Deprecated
    @Override
    public int openGateLeftAways(DevReturn devReturn) {
        MyDevReturn[] mydevReturn1 = creatMyDevReturnArray();
//        int ret = IGateDev_M810.mInstance.iOpenGate(mHandle, 1, 0, mydevReturn1);
        int ret = openGate(mHandle, 1, devReturn);
        filldevReturn(devReturn, mydevReturn1);
        return ret;
    }

    @Override
    public int openGateRightAways(DevReturn devReturn) {
        MyDevReturn[] mydevReturn1 = creatMyDevReturnArray();
        int ret = openGate(mHandle, 2, devReturn);
        filldevReturn(devReturn, mydevReturn1);
        return ret;
    }

    @Override
    public int openGateRightOnce(DevReturn devReturn) {
//        MyDevReturn[] mydevReturn1 = creatMyDevReturnArray();
//        int ret = openGate(mHandle, 2, devReturn);
//        filldevReturn(devReturn, mydevReturn1);
        mPassTimeOutBean.setRightTimeOut(System.currentTimeMillis());
        mPassTimeOutBean.setRightopened(true);
        mPassTimeOutBean.setOpenRightTimes(1);
//        getIGateMachineActionCallBack().openRight(ret == 0);

        return 0;
    }

    private int openGate(int p_hDevHandle, int dir, DevReturn devReturn) {
        MyDevReturn[] mydevReturn1 = creatMyDevReturnArray();
        int ret = IGateDev_M820.mInstance.iOpenGate(p_hDevHandle, dir, mydevReturn1);
        filldevReturn(devReturn, mydevReturn1);
        System.out.println("开门openGate " + dir);
        return ret;
    }

    @Deprecated
    @Override
    public int closeGate(DevReturn devReturn) {

        MyDevReturn[] mydevReturn1 = creatMyDevReturnArray();
        int ret = IGateDev_M820.mInstance.iCloseGate(mHandle, mydevReturn1);
        filldevReturn(devReturn, mydevReturn1);
        return ret;
    }

    @Override
    public int closeGate(int dir, DevReturn devReturn) {
//        MyDevReturn[] mydevReturn1 = creatMyDevReturnArray();
//        int ret = IGateDev_M820.mInstance.iCloseGate(mHandle, mydevReturn1);
//        filldevReturn(devReturn, mydevReturn1);
//
        if (dir == 1) {
            mPassTimeOutBean.setOpenLeftTimes(0);
//            getIGateMachineActionCallBack().closeLeft(ret == 0);
//
        } else {
            mPassTimeOutBean.setOpenRightTimes(0);
//            getIGateMachineActionCallBack().closeRight(ret == 0);
        }
        return 0;
    }


    @Override
    public int getPassageNum(TJZNGateDev_Passage_Num num, DevReturn devReturn) {

        int ret;
        MyDevReturn[] mydevReturn = creatMyDevReturnArray();
        TJZNGateDev_Passage_Num.ByReference passageNum = new TJZNGateDev_Passage_Num.ByReference();
        ret = IGateDev_M820.mInstance.iGetPassageNum(mHandle, passageNum, mydevReturn);
        num.passeNumL = passageNum.passeNumL;
        num.unPassNumL = passageNum.unPassNumL;
        num.timeoutNumL = passageNum.timeoutNumL;
        num.passeNumR = passageNum.passeNumR;
        num.unPassNumR = passageNum.unPassNumR;
        num.timeoutNumR = passageNum.timeoutNumR;
        filldevReturn(devReturn, mydevReturn);


        return ret;
    }


    @Override
    public int open(Context context) {
        return 0;
    }

    @Override
    public int close() {
        if (mGetPassByNumThread != null) {
            mGetPassByNumThread.interrupt();
            mGetPassByNumThread = null;
        }
        return 0;
    }


    private void filldevReturn(DevReturn devReturn, MyDevReturn[] mydevReturn) {
        devReturn.iLogicCode = mydevReturn[0].iLogicCode;

        devReturn.iPhyCode = mydevReturn[0].iPhyCode;
    }

    private MyDevReturn[] creatMyDevReturnArray() {
        return (MyDevReturn[]) new MyDevReturn().toArray(8);
    }


    class GetPassByNumThread extends Thread {
        private IGateMachineActionCallBack mCallBack;
        private IGateDev_M820 mGatemachine;
        private int mHandle = -1;
        private DevReturn mDevreturn = new DevReturn();
        private MyDevReturn[] mydevReturn = (MyDevReturn[]) new MyDevReturn().toArray(8);
        private TJZNGateDev_Passage_Num.ByReference lastTimepassageNum = new TJZNGateDev_Passage_Num.ByReference();

        public GetPassByNumThread(IGateMachineActionCallBack callBack, IGateDev_M820 gatemachine, int handle) {
            this.mCallBack = callBack;
            this.mGatemachine = gatemachine;
            mHandle = handle;
        }

        private int ret = -1;

        @Override
        public void run() {
            super.run();
            if (mGatemachine == null || mCallBack == null) {
                throw new NullPointerException("请先初始化 IGateDev_M820  或者设置回调 IGateMachineActionCallBack");
            }
            while (true) {

                try {
                    sleep(600);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //=====================开门回调=======================
                if (mPassTimeOutBean.isLeftopened() && mPassTimeOutBean.getOpenLeftTimes() >= 1) {
                    System.err.println("开门Leftopened");
                    int res = openGate(mHandle, 1, mDevreturn);
                    mPassTimeOutBean.setLeftopened(false);
                    mPassTimeOutBean.setLeftopenSuccess(res == 0);
                    mCallBack.openLeft(res == 0);
                }
                if (mPassTimeOutBean.isRightopened() && mPassTimeOutBean.getOpenRightTimes() >= 1) {
                    System.err.println("开门Rightopened");
                    int res = openGate(mHandle, 2, mDevreturn);
                    mPassTimeOutBean.setRightopened(false);
                    mPassTimeOutBean.setRightopenSuccess(res == 0);
                    mCallBack.openRight(res == 0);
                }
                //=====================开门=======================

                //=====================处理超时关门=======================
                //目前在闸机机芯端控制超时，以下代码目前不需要
//                if (mPassTimeOutBean.isLeftopenSuccess() && System.currentTimeMillis()
//                        - mPassTimeOutBean.getLeftTimeOut() >= getTimeout() * 1000) {
//                    closeGate(1, mDevreturn);
//                    mCallBack.passLeftTimeout();
//                    System.err.println("处理超时左关门");
//                }
//                if (mPassTimeOutBean.isRightopenSuccess() && System.currentTimeMillis()
//                        - mPassTimeOutBean.getRightTimeOut() >= getTimeout() * 1000) {
//                    closeGate(0, mDevreturn);
//                    mCallBack.passRightTimeout();
//                    System.err.println("处理超时右关门");
//                }
                //=====================处理超时关门=======================

                //=====================超时，通过成功回调=======================
                TJZNGateDev_Passage_Num.ByReference passageNum = new TJZNGateDev_Passage_Num.ByReference();
                ret = mGatemachine.iGetPassageNum(mHandle, passageNum, mydevReturn);
                if (passageNum.passeNumL - lastTimepassageNum.passeNumL >= 1) {
                    mCallBack.passLeftSuccess();
                    mPassTimeOutBean.setLeftopenSuccess(false);
                    int res = mPassTimeOutBean.getOpenLeftTimes();
                    int res1 = --res;
                    if (res1 <= 0) {
                        mCallBack.passLeftFinish();
                    }
                    mPassTimeOutBean.setLeftopened(res1 > 0);

                    mPassTimeOutBean.setOpenLeftTimes(res1);
//                    closeGate(1, mDevreturn);
                    System.err.println("左过闸成功");
                    System.err.println("passageNum:" + passageNum.toString());

                    DatabaseInstance.mDatabaseInstance.insert(EntyType.LEFTPASS, new LeftPass(null, null, SystemUtil.getYMD()));

                }
                if (passageNum.passeNumR - lastTimepassageNum.passeNumR >= 1) {
                    mCallBack.passRightSuccess();
                    mPassTimeOutBean.setRightopenSuccess(false);
                    int res = mPassTimeOutBean.getOpenRightTimes();
                    int res1 = --res;
                    if (res1 <= 0) {
                        mCallBack.passRightFinish();
                    }

                    mPassTimeOutBean.setRightopened(res1 > 0);
                    mPassTimeOutBean.setOpenRightTimes(res1);

//                    closeGate(0, mDevreturn);
                    System.err.println("右过闸成功");
                    System.err.println("passageNum:" + passageNum.toString());
                    DatabaseInstance.mDatabaseInstance.insert(EntyType.RIGHTPASS, new RightPass(null, null, SystemUtil.getYMD()));

                }
                if (passageNum.timeoutNumL - lastTimepassageNum.timeoutNumL >= 1) {
                    mCallBack.passLeftTimeout();
                    mPassTimeOutBean.setLeftopenSuccess(false);
                    int res = mPassTimeOutBean.getOpenLeftTimes();
                    int res1 = --res;
                    if (res1 <= 0) {
                        mCallBack.passLeftFinish();
                    }
                    mPassTimeOutBean.setLeftopened(res1 > 0);
                    mPassTimeOutBean.setOpenLeftTimes(res1);
                    System.err.println("左人数判断超时回调" + "res:" + res + " res1:" + res1);
                    System.err.println("passageNum:" + passageNum.toString());
                    DatabaseInstance.mDatabaseInstance.insert(EntyType.LEFTUNPASS, new LeftUnPass(null, null, SystemUtil.getYMD()));


                }
                if (passageNum.timeoutNumR - lastTimepassageNum.timeoutNumR >= 1) {
                    mCallBack.passRightTimeout();
                    mPassTimeOutBean.setRightopenSuccess(false);
                    int res = mPassTimeOutBean.getOpenRightTimes();
                    int res1 = --res;
                    if (res1 <= 0) {
                        mCallBack.passRightFinish();
                    }
                    mPassTimeOutBean.setRightopened(res1 > 0);
                    mPassTimeOutBean.setOpenRightTimes(res1);
                    System.err.println("右人数判断超时回调");
                    System.err.println("passageNum:" + passageNum.toString());
                    DatabaseInstance.mDatabaseInstance.insert(EntyType.RIGHTUNPASS, new RightUnPass(null, null, SystemUtil.getYMD()));

                }
//                System.err.println("passageNum:"+passageNum.toString());
                lastTimepassageNum = passageNum;
                //=====================超时，通过成功=======================
            }
        }
    }

}


