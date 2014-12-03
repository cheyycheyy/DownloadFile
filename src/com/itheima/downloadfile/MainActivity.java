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
						"���������󣬴����룺" + (String) msg.obj, 0).show();
				break;

			case DOWN_LOAD_ERROR:
				Toast.makeText(MainActivity.this, "����ʧ��", 0).show();
				break;
			case DOWN_LOAD_SUCCESS:
				Toast.makeText(MainActivity.this, "�ļ�������ϣ���ʱ�ļ���ɾ��!", 0).show();
				break;
			case UPDATE_TEXT:
				tv_process.setText("��ǰ���ȣ�" + pb_download.getProgress() * 100
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
					// ���ӷ�������ȡһ���ļ����ڱ��ش���һ��������ͬ��С����ʱ�ļ�
					URL url = new URL(path);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setRequestMethod("GET");
					conn.setConnectTimeout(5000);
					int code = conn.getResponseCode();
					if (code == 200) {
						// ���������ص����ݳ��ȣ�ʵ���Ͼ����ļ��ĳ���
						int length = conn.getContentLength();
						System.out.println("�ļ��ĳ��� ��" + length);

						// ���ý����������ֵ
						pb_download.setMax(length - 1);

						// �ڱ��ش���һ����С���������ļ�һ����С����ʱ�ļ�
						RandomAccessFile raf = new RandomAccessFile(resource,
								mode);
						// ָ����ʱ�ļ��ĳ���
						raf.setLength(length);
						raf.close();

						// ������3���߳�ȥ������Դ
						// ����ƽ��ÿ���߳����ص��ļ���С
						int blockSize = length / threadCount;
						// ��������������̵߳ĸ���
						runningThread = threadCount;
						// ���ؽ��ȹ���
						currentProgress = 0;
						// ���ؽ���������
						pb_download.setProgress(0);
						System.out.println("blockSize = " + blockSize);
						for (int threadId = 1; threadId <= threadCount; threadId++) {
							// �̵߳Ŀ�ʼ�ͽ���λ��
							int startIndex = (threadId - 1) * blockSize;
							int endIndex = threadId * blockSize - 1;
							if (threadId == threadCount) {
								endIndex = length - 1;
							}
							System.out.println("�̣߳�" + threadId + "���أ�"
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

				// ����Ƿ���ڼ�¼���س��ȵ��ļ���������ڶ�ȡ����ļ�������
				File tempFile = new File(tempFileUrl);
				if (tempFile.exists() && tempFile.length() > 0) {
					FileInputStream fis = new FileInputStream(tempFile);
					byte[] buffer = new byte[1024];
					int length = fis.read(buffer);
					String downloadLength = new String(buffer, 0, length);
					int downloadLenInt = Integer.parseInt(downloadLength);

					int alreayDownloadInt = downloadLenInt - startIndex;
					currentProgress += alreayDownloadInt;// �����ϴζϵ����ص��Ľ���

					// �޸����ص���ʵ��ʼλ��
					startIndex = downloadLenInt;
					fis.close();
				}

				System.out.println("�߳�������ʼ��λ�� ��" + threadId + "���أ�"
						+ startIndex + "--->" + endIndex);

				URL url = new URL(path);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setRequestMethod("GET");
				conn.setConnectTimeout(5000);
				// ������������ز����ļ���ָ���ļ���λ��
				conn.setRequestProperty("Range", "bytes=" + startIndex + "-"
						+ endIndex);
				conn.setConnectTimeout(60000);

				// 200��ʾ�ӷ���������ȫ����Դ��206��ʾ�ӷ��������󲿷���Դ
				int code = conn.getResponseCode();
				if (code == 206) {
					// �Ѿ������������λ�ã����ص��ǵ�ǰ�ļ�λ�ö�Ӧ���ļ���������
					InputStream is = conn.getInputStream();
					// �����ʵ��֧�ֶ���������ļ��Ķ�ȡ��д��
					RandomAccessFile raf = new RandomAccessFile(resource, mode);
					// ��λ�ļ�
					raf.seek(startIndex);

					int length = 0;
					byte[] buffer = new byte[1024];
					int total = 0;// ʵʱ��¼�Ѿ����صĳ���
					// һ����
					while ((length = is.read(buffer)) != -1) {

						RandomAccessFile file = new RandomAccessFile(
								tempFileUrl, mode);

						raf.write(buffer, 0, length);
						total += length;

						// ��¼��ǰ�߳����ص�����λ��
						//file.write((total + startIndex - 1 + "").getBytes());
						file.write((total + startIndex + "").getBytes());//����Ϊ����Ҫ��1
						file.close();

						synchronized (MainActivity.this) {
							// ��ȡ�����߳����ص��ܽ���
							currentProgress += length;
							// ���Ľ�����progressbar�Ľ���(progressbar��progressdialog���������߳����޸�UI)
							pb_download.setProgress(currentProgress);

							Message msg = Message.obtain();
							msg.what = UPDATE_TEXT;
							handler.sendMessage(msg);
						}
					}

					raf.close();
					is.close();

					System.out.println("�߳� �� " + threadId + "������ϡ���������");
				} else {
					System.out.println("�߳� �� " + threadId + "����ʧ�ܡ���������");
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("�߳� �� " + threadId + "  �׳��쳣��");
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
					deleteFile.delete();//���߳�������Ϻ�ɾ����ʱ�ļ�
				}

				Message msg = new Message();
				msg.what = DOWN_LOAD_SUCCESS;
				handler.sendMessage(msg);

			}
		}
	}

}