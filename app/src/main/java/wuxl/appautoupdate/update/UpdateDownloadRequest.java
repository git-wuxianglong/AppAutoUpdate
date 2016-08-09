package wuxl.appautoupdate.update;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 负责文件的下载和线程间的通信
 * Created by wuxl_
 * time：2016/8/8 16:08
 */
public class UpdateDownloadRequest implements Runnable {

    private String downloadUrl;
    private String localFiledPath;
    private UpdateDownloadListener downloadListener;
    private boolean isDownloading = false;
    private long currentLength;

    private DownloadResponseHandler downloadResponseHandler;

    private static final String TAG = "TAG";

    public UpdateDownloadRequest(String downloadUrl, String localFiledPath, UpdateDownloadListener downloadListener) {
        this.downloadUrl = downloadUrl;
        this.localFiledPath = localFiledPath;
        this.downloadListener = downloadListener;
        this.isDownloading = true;
        this.downloadResponseHandler = new DownloadResponseHandler();
    }

    //建立连接
    private void makeRequest() throws IOException, InterruptedException {
        if (!Thread.currentThread().isInterrupted()) {
            try {
                URL url = new URL(downloadUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.connect();//阻塞我们当前的线程
                currentLength = connection.getContentLength();
                if (!Thread.currentThread().isInterrupted()) {
                    //完成文件的下载
                    downloadResponseHandler.sendResponseMessage(connection.getInputStream());
                }
            } catch (IOException e) {
                throw e;
            }
        }
    }

    @Override
    public void run() {
        try {
            makeRequest();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 包含了下载过程中所有可能出现的异常情况
     */
    public enum FailureCode {
        UnknownHost, Socket, SocketTimeout, ConnectTimeout, IO, HttpResponse, Json, Interrupted
    }

    /**
     * 用来真正的去下载文件，并发送消息和回调的接口
     */
    public class DownloadResponseHandler {

        protected static final int SUCCESS_MESSAGE = 0;
        protected static final int FAILURE_MESSAGE = 1;
        protected static final int START_MESSAGE = 2;
        protected static final int FINISH_MESSAGE = 3;
        protected static final int NETWORK_OFF = 4;
        private static final int PROGRESS_CHANGED = 5;

        private int mCompleteSize = 0;
        private int progress = 0;

        private Handler handler;//完成线程间的通信

        public DownloadResponseHandler() {
            handler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    handleSelfMessage(msg);
                }
            };
        }

        /**
         * 用来发送不同的消息对象
         */
        protected void sendFinishMessage() {
            sendMessage(obtainMessage(FINISH_MESSAGE, null));
        }

        private void sendProgressChangedMessage(int progress) {
            sendMessage(obtainMessage(PROGRESS_CHANGED, new Object[]{progress}));
        }

        protected void sendFailureMessage(FailureCode failureCode) {
            sendMessage(obtainMessage(FAILURE_MESSAGE, new Object[]{failureCode}));
        }

        protected void sendMessage(Message msg) {
            if (handler != null) {
                handler.sendMessage(msg);
            } else {
                handleSelfMessage(msg);
            }

        }

        /**
         * 获取一个消息对象
         *
         * @param responseMessage
         * @param response
         * @return
         */
        protected Message obtainMessage(int responseMessage, Object response) {
            Message msg;
            if (handler != null) {
                msg = handler.obtainMessage(responseMessage, response);
            } else {
                msg = Message.obtain();
                msg.what = responseMessage;
                msg.obj = response;
            }
            return msg;

        }

        protected void handleSelfMessage(Message msg) {
            Object[] response;
            switch (msg.what) {
                case FAILURE_MESSAGE:
                    response = (Object[]) msg.obj;
                    handlerFailureMessage((FailureCode) response[0]);
                    break;
                case PROGRESS_CHANGED:
                    response = (Object[]) msg.obj;
                    int p = ((Integer) response[0]).intValue();
                    handlerProgressChangedMessage(p);
                    break;
                case FINISH_MESSAGE:
                    onFinish();
                    break;
            }
        }

        //各种消息的处理逻辑
        protected void handlerProgressChangedMessage(int progress) {
            downloadListener.onProgressChanged(progress, "");
        }

        protected void handlerFailureMessage(FailureCode failureCode) {
            onFailure(failureCode);
        }

        public void onFinish() {
            downloadListener.onFinished(mCompleteSize, "");
        }

        public void onFailure(FailureCode failureCode) {
            downloadListener.onFailure();
        }

        /**
         * 文件下载方法
         *
         * @param is
         */
        void sendResponseMessage(InputStream is) {
            RandomAccessFile randomAccessFile = null;
            mCompleteSize = 0;
            try {
                byte[] buffer = new byte[1024];
                int length = -1;
                int limit = 0;
                randomAccessFile = new RandomAccessFile(localFiledPath, "rwd");
                Log.i(TAG, "currentLength: " + currentLength);
                while ((length = is.read(buffer)) != -1) {
                    if (isDownloading) {
                        randomAccessFile.write(buffer, 0, length);
                        mCompleteSize += length;
                        if (mCompleteSize < currentLength) {
                            progress = (int) (mCompleteSize * 100 / currentLength);
                            if (limit % 30 == 0 && progress <= 100) {
                                //为了限制一下notification的更新频率
                                sendProgressChangedMessage(progress);
                            }

                            if (progress >= 100) {
                                //下载完成
                                sendProgressChangedMessage(progress);
                            }

                            limit++;
                        }
                    }
                }
                sendFinishMessage();
            } catch (IOException e) {
                sendFailureMessage(FailureCode.IO);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }

                    if (randomAccessFile != null) {
                        randomAccessFile.close();
                    }
                } catch (IOException e) {
                    sendFailureMessage(FailureCode.IO);
                }
            }
        }
    }
}
