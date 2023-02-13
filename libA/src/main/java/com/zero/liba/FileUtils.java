package com.zero.liba;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;

/**
 * @Describe 文件操作工具集
 * @author zero
 * @time 2016-7-2下午5:19:13
 */
public class FileUtils {
	/**
	 * 获取SD卡路径
	 */
	public static String getSDCardPath() {
		return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
	}

	/**
	 * 判断SD卡是否可用
	 */
	public static boolean isSDCardAvailable() {
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			return true;
		}
		return false;
	}

	/**
	 * 获取SD卡剩余空间
	 */
	@SuppressWarnings("deprecation")
	public static long getSDFreeSize() {
		if (isSDCardAvailable()) {
			StatFs statFs = new StatFs(getSDCardPath());

			long blockSize = statFs.getBlockSize();

			long freeBlocks = statFs.getAvailableBlocks();
			return freeBlocks * blockSize;
		}

		return 0;
	}

	/**
	 * 获取SD卡的总容量
	 */
	@SuppressWarnings("deprecation")
	public static long getSDAllSize() {
		if (isSDCardAvailable()) {
			StatFs stat = new StatFs(getSDCardPath());
			// 获取空闲的数据块的数量
			long availableBlocks = (long) stat.getAvailableBlocks() - 4;
			// 获取单个数据块的大小（byte）
			long freeBlocks = stat.getAvailableBlocks();
			return freeBlocks * availableBlocks;
		}
		return 0;
	}

	/**
	 * 获取指定路径所在空间的剩余可用容量字节数
	 * 
	 * @param filePath
	 * @return 容量字节 SDCard可用空间，内部存储可用空间
	 */
	@SuppressWarnings("deprecation")
	public static long getFreeBytes(String filePath) {
		// 如果是sd卡的下的路径，则获取sd卡可用容量
		if (filePath.startsWith(getSDCardPath())) {
			filePath = getSDCardPath();
		} else {// 如果是内部存储的路径，则获取内存存储的可用容量
			filePath = Environment.getDataDirectory().getAbsolutePath();
		}
		StatFs stat = new StatFs(filePath);
		long availableBlocks = (long) stat.getAvailableBlocks() - 4;
		return stat.getBlockSize() * availableBlocks;
	}

	/**
	 * 拷贝文件，通过返回值判断是否拷贝成功
	 * 
	 * @param sourcePath
	 *            源文件路径
	 * @param targetPath
	 *            目标文件路径
	 */
	public static boolean copyFile(String sourcePath, String targetPath) {
		boolean isOK = false;
		if (!TextUtils.isEmpty(sourcePath) && !TextUtils.isEmpty(targetPath)) {
			File sourcefile = new File(sourcePath);
			File targetFile = new File(targetPath);
			if (!sourcefile.exists()) {
				return false;
			}
			if (sourcefile.isDirectory()) {
				isOK = copyDir(sourcefile, targetFile);
			} else if (sourcefile.isFile()) {
				if (!targetFile.exists()) {
					createFile(targetPath);
				}
				FileOutputStream outputStream = null;
				FileInputStream inputStream = null;
				try {
					inputStream = new FileInputStream(sourcefile);
					outputStream = new FileOutputStream(targetFile);
					byte[] bs = new byte[1024];
					int len;
					while ((len = inputStream.read(bs)) != -1) {
						outputStream.write(bs, 0, len);
					}
					isOK = true;
				} catch (Exception e) {
					e.printStackTrace();
					isOK = false;
				} finally {
					if (inputStream != null) {
						try {
							inputStream.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					if (outputStream != null) {
						try {
							outputStream.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}

			return isOK;
		}
		return false;
	}

	/**
	 * 删除文件
	 */
	public static boolean deleteFile(String path) {
		if (!TextUtils.isEmpty(path)) {
			File file = new File(path);
			if (!file.exists()) {
				return false;
			}
			try {
				file.delete();
			} catch (Exception e) {
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * 根据指定路径，创建父目录及文件
	 * 
	 * @param filePath
	 * @return File 如果创建失败的话，返回null
	 */
	public static File createFile(String filePath) {
		return createFile(filePath, "755");
	}

	/**
	 * 创建文件，并修改读写权限
	 */
	public static File createFile(String filePath, String mode) {
		File desFile = null;
		try {
			String desDir = filePath.substring(0, filePath.lastIndexOf(File.separator));
			File dir = new File(desDir);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			chmodFile(dir.getAbsolutePath(), mode);
			desFile = new File(filePath);
			if (!desFile.exists()) {
				desFile.createNewFile();
			}
			chmodFile(desFile.getAbsolutePath(), mode);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return desFile;
	}

	/**
	 * 修改文件读写权限
	 */
	public static void chmodFile(String fileAbsPath, String mode) {
		String cmd = "chmod " + mode + " " + fileAbsPath;
		try {
			Runtime.getRuntime().exec(cmd);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 拷贝目录
	 * 
	 * @param sourceFile
	 * @param targetFile
	 * @return
	 */
	public static boolean copyDir(File sourceFile, File targetFile) {
		if (sourceFile == null || targetFile == null) {
			return false;
		}
		if (!sourceFile.exists()) {
			return false;
		}
		if (!targetFile.exists()) {
			targetFile.mkdirs();
		}
		// 获取目录下所有文件和文件夹的列表
		File[] files = sourceFile.listFiles();
		if (files == null || files.length < 1) {
			return false;
		}
		File file = null;
		StringBuffer buffer = new StringBuffer();
		boolean isSuccessful = false;
		// 遍历目录下的所有文件文件夹，分别处理
		for (int i = 0; i < files.length; i++) {
			file = files[i];
			buffer.setLength(0);
			buffer.append(targetFile.getAbsolutePath()).append(File.separator)
					.append(file.getName());
			if (file.isFile()) {
				// 文件直接调用拷贝文件方法
				isSuccessful = copyFile(file.getAbsolutePath(), buffer.toString());
				if (!isSuccessful) {
					return false;
				}
			} else if (file.isDirectory()) {
				// 目录再次调用拷贝目录方法
				copyDir(file, new File(buffer.toString()));
			}

		}
		return true;
	}

	/**
	 * 统计文件夹文件的大小
	 */
	public static long getSize(File file) {
		// 判断文件是否存在
		if (file.exists()) {
			// 如果是目录则递归计算其内容的总大小，如果是文件则直接返回其大小
			if (!file.isFile()) {
				// 获取文件大小
				File[] fl = file.listFiles();
				long ss = 0;
				for (File f : fl)
					ss += getSize(f);
				return ss;
			} else {
				long ss = (long) file.length();
				return ss; // 单位制bytes
			}
		} else {
			// System.out.println("文件或者文件夹不存在，请检查路径是否正确！");
			return 0;
		}
	}

	/**
	 * 把bytes转换成MB
	 */
	public static String getTrafficStr(long total) {
		DecimalFormat format = new DecimalFormat("##0.0");
		if (total < 1024 * 1024) {
			return format.format(total / 1024f) + "KB";
		} else if (total < 1024 * 1024 * 1024) {
			return format.format(total / 1024f / 1024f) + "MB";
		} else if (total < 1024 * 1024 * 1024 * 1024) {
			return format.format(total / 1024f / 1024f / 1024f) + "GB";
		} else {
			return "统计错误";
		}
	}

	/**
	 * 删除文件夹里面的所以文件
	 */
	public static void deleteDir(File dir) {
		if (dir.exists()) {
			File[] files = dir.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isFile()) {
					files[i].delete();
				} else {
					deleteDir(files[i]);
				}
			}
		}
	}

	/**
	 * 获取系统存储路径
	 * 
	 * @return
	 */
	public static String getRootDirectoryPath() {
		return Environment.getRootDirectory().getAbsolutePath();
	}

	/**
	 * 获取外部存储路径
	 * 
	 * @return
	 */
	public static String getExternalStorageDirectoryPath() {
		return Environment.getExternalStorageDirectory().getPath();
	}

	/**
	 * 将object对象写入outFile文件
	 * 
	 * @param outFile
	 * @param object
	 * @param context
	 */
	public static void writeObject2File(String outFile, Object object, Context context) {
		ObjectOutputStream out = null;
		FileOutputStream outStream = null;
		try {
			File dir = context.getDir("cache", Context.MODE_PRIVATE);
			outStream = new FileOutputStream(new File(dir, outFile));
			out = new ObjectOutputStream(new BufferedOutputStream(outStream));
			out.writeObject(object);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (outStream != null) {
				try {
					outStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 从outFile文件读取对象
	 * 
	 * @param filePath
	 * @param context
	 */
	public static Object readObjectFromPath(String filePath, Context context) {
		Object object = null;
		ObjectInputStream in = null;
		FileInputStream inputStream = null;
		try {
			File dir = context.getDir("cache", Context.MODE_PRIVATE);
			File f = new File(dir, filePath);
			if (f == null || !f.exists()) {
				return null;
			}
			inputStream = new FileInputStream(new File(dir, filePath));
			in = new ObjectInputStream(new BufferedInputStream(inputStream));
			object = in.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		return object;
	}

	/**
	 * 读取指定路径下的文件内容
	 */
	public static String readFile(String path) {
		BufferedReader br = null;
		try {
			File myFile = new File(path);
			br = new BufferedReader(new FileReader(myFile));
			StringBuffer sb = new StringBuffer();
			String line = br.readLine();
			while (line != null) {
				sb.append(line);
				line = br.readLine();
			}
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	/**
	 * 写入文件内容
	 */
	public boolean writeFile(String path,String content){
		FileWriter fw = null;
		try {
			fw = new FileWriter(new File(path));
			fw.write(content,0,content.length());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally{
			try {
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
}
