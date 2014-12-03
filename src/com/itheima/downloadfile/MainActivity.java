package com.itheima.downloadfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	protected static final int SERVER_ERROR = 1;
	protected static final int DOWN_LOAD_ERROR = 2;
	protected static final int DOWN_LOAD_SUCCESS = 3;
	protected static final int UPDATE_TEXT = 4;

	private EditText et_path;
	private ProgressBar pb_download;
	private TextView tv_process;

	private static int threadCount = 3;
	private static int runningThread = threadCount;
	private static int currentProgress = 0;

	private static String resource = "/mnt/sdcard/360sd_std_5.0.0.5103.apk";
	private static String path = "http://36.250.4.20/dd.myapp.com/16891/B1960F60646DC3A8086AC59ADA1F55C4.apk?mkey=5469b6f7ceb96785&f=d688&fsname=com%2Etencent%2Emobileqq%5F5%2E2%2E1%5F182.apk&asr=8eff&p=.apk";
	private static String mode = "rwd";

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SERVER_ERROR:
				Toast.makeText(MainActivity.this,
						"服务器错误，错误码：" + (String) msg.obj, 0).show();
				break;

			case DOWN_LOAD_ERROR:
				Toast.makeText(MainActivity.this, "下载失败", 0).show();
				break;
			case DOWN_LOAD_SUCCESS:
				Toast.makeText(MainActivity.this, "文件下载完毕，临时文件被删除!", 0).show();
				break;
			case UPDATE_TEXT:
				tv_process.setText("当前进度：" + pb_download.getProgress() * 100
						/ pb_download.getMax());
				break;
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.et_path = (EditText) this.findViewById(R.id.et_path);
		this.pb_download = (ProgressBar) this.findViewById(R.id.pb_download);
		this.tv_process = (TextView) this.findViewById(R.id.tv_process);
	}

	public void download(View view) {
		new Thread() {

			public void run() {
				try {
					// 连接服务器获取一个文件，在本地创建一个和它相同大小的临时文件
					URL url = new URL(path);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setRequestMethod("GET");
					conn.setConnectTimeout(5000);
					int code = conn.getResponseCode();
					if (code == 200) {
						// 服务器返回的数据长度，实际上就是文件的长度
						int length = conn.getContentLength();
						System.out.println("文件的长度 ：" + length);

						// 设置进度条的最大值
						pb_download.setMax(length - 1);

						// 在本地创建一个大小跟服务器文件一样大小的临时文件
						RandomAccessFile raf = new RandomAccessFile(resource,
								mode);
						// 指定临时文件的长度
						raf.setLength(length);
						raf.close();

						// 假设是3个线程去下载资源
						// 计算平均每个线程下载的文件大小
						int blockSize = length / threadCount;
						// 标记正在运行子线程的个数
						runningThread = threadCount;
						// 下载进度归零
						currentProgress = 0;
						// 下载进度条归零
						pb_download.setProgress(0);
						System.out.println("blockSize = " + blockSize);
						for (int threadId = 1; threadId <= threadCount; threadId++) {
							// 线程的开始和结束位置
							int startIndex = (threadId - 1) * blockSize;
							int endIndex = threadId * blockSize - 1;
							if (threadId == threadCount) {
								endIndex = length - 1;
							}
							System.out.println("线程：" + threadId + "下载："
									+ startIndex + "--->" + endIndex);
							new DownLoadThread(threadId, startIndex, endIndex,
									path).start();
						}
					} else {
						Message msg = new Message();
						msg.what = SERVER_ERROR;
						msg.obj = code + "";
						handler.sendMessage(msg);
					}

				} catch (Exception e) {
					e.printStackTrace();
					Message msg = new Message();
					msg.what = DOWN_LOAD_ERROR;
					handler.sendMessage(msg);
				}

			};
		}.start();
	}

	public class DownLoadThread extends Thread {

		private int threadId;
		private int startIndex;
		private int endIndex;
		private String path;
		private String tempFileUrl;

		public DownLoadThread(int threadId, int startIndex, int endIndex,
				String path) {
			this.threadId = threadId;
			this.startIndex = startIndex;
			this.endIndex = endIndex;
			this.path = path;
			tempFileUrl = "/mnt/sdcard/" + threadId + ".txt";
		}

		@Override
		public void run() {
			try {

				// 检查是否存在记录下载长度的文件，如果存在读取这个文件的数据
				File tempFile = new File(tempFileUrl);
				if (tempFile.exists() && tempFile.length() > 0) {
					FileInputStream fis = new FileInputStream(tempFile);
					byte[] buffer = new byte[1024];
					int length = fis.read(buffer);
					String downloadLength = new String(buffer, 0, length);
					int downloadLenInt = Integer.parseInt(downloadLength);

					int alreayDownloadInt = downloadLenInt - startIndex;
					currentProgress += alreayDownloadInt;// 计算上次断点下载到的进度

					// 修改下载的真实开始位置
					startIndex = downloadLenInt;
					fis.close();
				}

				System.out.println("线程真正开始的位置 ：" + threadId + "下载："
						+ startIndex + "--->" + endIndex);

				URL url = new URL(path);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setRequestMethod("GET");
				conn.setConnectTimeout(5000);
				// 请求服务器下载部分文件，指定文件的位置
				conn.setRequestProperty("Range", "bytes=" + startIndex + "-"
						+ endIndex);
				conn.setConnectTimeout(60000);

				// 200表示从服务器请求全部资源，206表示从服务器请求部分资源
				int code = conn.getResponseCode();
				if (code == 206) {
					// 已经设置了请求的位置，返回的是当前文件位置对应的文件的输入流
					InputStream is = conn.getInputStream();
					// 此类的实例支持对随机访问文件的读取和写入
					RandomAccessFile raf = new RandomAccessFile(resource, mode);
					// 定位文件
					raf.seek(startIndex);

					int length = 0;
					byte[] buffer = new byte[1024];
					int total = 0;// 实时记录已经下载的长度
					// 一阵狂读
					while ((length = is.read(buffer)) != -1) {

						RandomAccessFile file = new RandomAccessFile(
								tempFileUrl, mode);

						raf.write(buffer, 0, length);
						total += length;

						// 记录当前线程下载的数据位置
						//file.write((total + startIndex - 1 + "").getBytes());
						file.write((total + startIndex + "").getBytes());//我认为不需要减1
						file.close();

						synchronized (MainActivity.this) {
							// 获取所有线程下载的总进度
							currentProgress += length;
							// 更改界面上progressbar的进度(progressbar和progressdialog可以在子线程里修改UI)
							pb_download.setProgress(currentProgress);

							Message msg = Message.obtain();
							msg.what = UPDATE_TEXT;
							handler.sendMessage(msg);
						}
					}

					raf.close();
					is.close();

					System.out.println("线程 ： " + threadId + "下载完毕。。。。。");
				} else {
					System.out.println("线程 ： " + threadId + "下载失败。。。。。");
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("线程 ： " + threadId + "  抛出异常！");
				runningThread = threadCount;
			} finally {
				System.out.println("runningThread = " + runningThread);
				threadFinish();

			}
		}

		private synchronized void threadFinish() {
			runningThread--;
			if (runningThread == 0) {
				for (int i = 1; i <= threadCount; i++) {
					File deleteFile = new File("/mnt/sdcard/" + i + ".txt");
					deleteFile.delete();//当线程下载完毕后删除临时文件
				}

				Message msg = new Message();
				msg.what = DOWN_LOAD_SUCCESS;
				handler.sendMessage(msg);

			}
		}
	}

}