package com.zero.liba;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @Describe 操作APK的工具集
 * @author zero
 * @time 2016-6-25下午1:10:13
 */
public class ApkUtils {
	
	/**
	 * 获取当前的Api
	 */
	public static int getCurrentApi(){
		return Build.VERSION.SDK_INT;
	}
	
	/**
	 * 获取应用程序名称
	 */
	public static String getAppName(Context context) {
		try {
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
			int labelRes = packageInfo.applicationInfo.labelRes;
			return context.getResources().getString(labelRes);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取当前应用的版本名称
	 */
	public static String getVersionName(Context context) {
		try {
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取当前程序的版本号
	 */
	public static int getVersionCode(Context context) {
		int version = 0;
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
			version = packageInfo.versionCode;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return version;
	}

	/**
	 * 安装APK
	 */
	public static void installApk(Context context, File file) {
		// 获取文件的Uri
		Uri uri = Uri.fromFile(file);
		Intent installIntent = new Intent(Intent.ACTION_VIEW);
		installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// 设置intent的数据类型
		installIntent.setDataAndType(uri, "application/vnd.android.package-archive");
		context.startActivity(installIntent);
	}

	/**
	 * 卸载APK
	 */
	public static void unInstallApk(Context context, String packageName) {
		Intent i = new Intent();
		Uri uri = Uri.parse("package:" + packageName);
		// 获取删除包名的
		i.setAction(Intent.ACTION_DELETE);
		// 设置我们要执行的卸载动作
		i.setData(uri);
		context.startActivity(i);
	}

	/**
	 * 获取所有非系统应用APP的包名
	 */
	public static List<String> getInstalledAppList(Context context) {
		List<String> allApps = new ArrayList<String>();
		PackageManager pm = context.getPackageManager();
		List<PackageInfo> packages = pm.getInstalledPackages(0);
		for (PackageInfo packageInfo : packages) {
			// 非系统应用
			if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
				allApps.add(packageInfo.packageName);
			} else {
				// 系统应用?
			}
		}
		return allApps;
	}

	/**
	 * 获取任务栈最顶端的Activity名，注：该方法只适用于5.0之前
	 */
	public static String getTopActivity(Context context) {
		ActivityManager manager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		/** 获取当前正在运行的任务栈列表， 越是靠近当前运行的任务栈会被排在第一位，之后的以此类推 */
		List<RunningTaskInfo> runningTasks = manager.getRunningTasks(1);
		/** 获得当前最顶端的任务栈，即前台任务栈 */
		RunningTaskInfo runningTaskInfo = runningTasks.get(0);
		/** 获取前台任务栈的最顶端 Activity */
		ComponentName topActivity = runningTaskInfo.topActivity;
		return topActivity.toString();
	}

	/**
	 * 获取任务栈最顶端的应用包名，注：该方法只适用于5.0之前
	 */
	public static String getTopPackage(Context context) {
		ActivityManager manager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		/** 获取当前正在运行的任务栈列表， 越是靠近当前运行的任务栈会被排在第一位，之后的以此类推 */
		List<RunningTaskInfo> runningTasks = manager.getRunningTasks(1);
		/** 获得当前最顶端的任务栈，即前台任务栈 */
		RunningTaskInfo runningTaskInfo = runningTasks.get(0);
		/** 获取前台任务栈的最顶端 Activity */
		ComponentName topActivity = runningTaskInfo.topActivity;
		/** 获取应用的包名 */
		return topActivity.getPackageName();
	}

	/**
	 * 获取任务栈最顶端的应用包名，注：该方法5.0前后都可以使用
	 */
	public static String getTopPackage2(Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		String processName = manager.getRunningAppProcesses().get(0).processName;
		return processName;
	}

	/**
	 * 启动应用
	 */
	public static void startApp(Context context, String packageName) {
		PackageManager packageManager = context.getPackageManager();
		Intent intent = packageManager.getLaunchIntentForPackage(packageName);
		context.startActivity(intent);
	}

	/**
	 * 判断系统是否已经root
	 */
	public static boolean isRootSystem() {
		File f = null;
		final String kSuSearchPaths[] = { "/system/bin/", "/system/xbin/", "/system/sbin/",
				"/sbin/", "/vendor/bin/" };
		for (int i = 0; i < kSuSearchPaths.length; i++) {
			f = new File(kSuSearchPaths[i] + "su");
			if (f != null && f.exists()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 安装APK，必须取得root权限，该方法才能生效
	 * 
	 * @param file
	 *            APK路径
	 * @return 1成功，2拒绝ROOT权限，3APK签名不一致，4APK包不完整，5文件不存在,6未知错误
	 */
	public static int installApk(File file) {
		int result = -1;
		if (file == null || !file.exists()) {
			return 5;
		}
		Process process = null;
		OutputStream out = null;
		InputStream in = null;
		InputStream error = null;
		String line = "";
		try {
			process = Runtime.getRuntime().exec("su");
			out = process.getOutputStream();
			out.write(("pm install -r " + file.getPath() + "\n").getBytes());
			out.write(("exit\n").getBytes());
			out.flush();
			process.waitFor();
			in = process.getInputStream();
			error = process.getErrorStream();
			int length = in.available();
			int errorLength = error.available();
			if (length == 0 && errorLength > 0) {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(error));
				do {
					line = bufferedReader.readLine();
					if (line == null) {
						result = 6;
						break;
					}
					if (line.equals("Failure [INSTALL_FAILED_INVALID_APK]")) {
						result = 4;
						break;
					}
					if (line.equals("Failure [INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES]")) {
						result = 3;
						break;
					}
				} while (true);
			} else if (length > 0) {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
				do {
					line = bufferedReader.readLine();
					if (null == line) {
						result = 6;
						break;
					}
					if (line.equals("Success")) {
						result = 1;
						break;
					}
				} while (true);
			} else if (length == 0 && errorLength == 0) {
				result = 2;
			}
		} catch (Exception e) {
			result = 2;
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
				if (process != null) {
					process.destroy();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 卸载APK，必须取得root权限，该方法才能生效
	 * 
	 * @param packageName
	 *            包名
	 * @return 1成功，2失败
	 */
	public static int unInstallApk(String packageName) {
		int result = -1;
		Process process = null;
		OutputStream out = null;
		InputStream in = null;
		String state = null;
		try {
			process = Runtime.getRuntime().exec("su");
			out = process.getOutputStream();
			out.write(("pm uninstall " + packageName + "\n").getBytes());
			out.write("exit\n".getBytes());
			out.flush();
			process.waitFor();
			in = process.getInputStream();
			int len = 0;
			byte[] bs = new byte[256];
			while ((len = in.read(bs)) != -1) {
				state = new String(bs, 0, len);
				if (state.equals("Success\n")) {
					result = 1;
					break;
				} else if (state.equals("Failure\n")) {
					result = 2;
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			result = 2;
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
				if (process != null) {
					process.destroy();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 获取物理MAC地址
	 */
	public static String getMac() {
		String result = "";
		String Mac = "";
		result = callCmd("busybox ifconfig", "HWaddr");

		if (result == null) {
			return "";
		}
		if (result.length() > 0 && result.contains("HWaddr")) {
			Mac = result.substring(result.indexOf("HWaddr") + 6, result.length() - 1);
			if (Mac.length() > 1) {
				result = Mac.toLowerCase();
			}
		}
		return result.trim();
	}

	private static String callCmd(String cmd, String filter) {
		String result = "";
		String line = "";
		try {
			Process proc = Runtime.getRuntime().exec(cmd);
			InputStreamReader is = new InputStreamReader(proc.getInputStream());
			BufferedReader br = new BufferedReader(is);

			// 执行命令cmd，只取结果中含有filter的这一行
			while ((line = br.readLine()) != null && line.contains(filter) == false) {
			}

			result = line;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

}
