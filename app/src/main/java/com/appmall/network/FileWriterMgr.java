package com.appmall.network;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import android.support.v4.util.SparseArrayCompat;

import java.util.HashMap;
import java.util.ArrayList;


public class FileWriterMgr {

	private SparseArrayCompat<FileWriter> mFileWriterList = new SparseArrayCompat<FileWriter>();
	private HashMap<Integer, Long> mTotalLengthMap = new HashMap<Integer, Long>();
	private HashMap<Integer, Long> mWriteLengthMap = new HashMap<Integer, Long>();
	
	public boolean onHandleData(int taskid, byte[] data, int len) {
		FileWriter fileWriter = mFileWriterList.get(taskid);
		if(fileWriter != null) {
			return fileWriter.onHandleData(data, len);
		}
		return false;
	}
	
	public void onHandleEndData(int taskid) {
		FileWriter fileWriter = mFileWriterList.get(taskid);
		if(fileWriter != null) {
			fileWriter.onHandleEndData();
			mFileWriterList.remove(taskid);
		}
	}
	
	public int getWriterCount() {
		return mFileWriterList.size();
	}
	
	public void setTotalLen(int taskid, long totalLen) {
		mTotalLengthMap.put(taskid, totalLen);
	}
	
	public long getTotalLen(int taskid) {
		if(mTotalLengthMap.get(taskid) == null)
			return 0;
		return mTotalLengthMap.get(taskid).longValue();
	}
	
	public synchronized void addWriteLen(int taskid, long len) {
		long writeLen = 0;
		if(mWriteLengthMap.get(taskid) == null)
			writeLen = len;
		else {
			writeLen += mWriteLengthMap.get(taskid).longValue()+ len;		
		}
		mWriteLengthMap.put(taskid, writeLen);
	}
	
	public long getWriteLen(int taskid) {
		if(mWriteLengthMap.get(taskid) == null)
			return 0;
		else
			return mWriteLengthMap.get(taskid).longValue();
	}
	
	private ArrayList<FileWriter> mFileWriterArray = new ArrayList<FileWriter>();
	public synchronized ArrayList<FileWriter> getFileWriterList(int taskid) {
		mFileWriterArray.clear();
		for(int i=0; i< mFileWriterList.size(); i++) {
			FileWriter writer = mFileWriterList.valueAt(i);
			if(writer != null && writer.mTaskid == taskid) {
				mFileWriterArray.add(writer);
			}
		}
		return mFileWriterArray;
	}
		
	public class FileWriter {
		private int mTaskid;
		private PrintStream mOuputStream;
		private RandomAccessFile mFile;
		public long mStartPos = 0;
		public long mEndPos = 0;
		
		public FileWriter(int taskid) {
			mTaskid = taskid;
		}
		
		private boolean writeFile(byte[] data, int len) {
			boolean bOk = true;
			try {
				mFile.seek(mStartPos);
				mOuputStream.write(data, 0, len);
				if (mOuputStream.checkError()) {
					bOk = false;
				}
				mStartPos += len;
				addWriteLen(mTaskid, (long)len);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return bOk;
		}
		
		public boolean onHandleData(byte[] data, int len) {
			boolean bOk = true;			
			bOk = writeFile(data, len);	
			return bOk;
		}
		
		public void onHandleEndData() {
			if (mOuputStream != null) {
				mOuputStream.flush();
				mOuputStream.close();
				try {
					mFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}	
		}
	}
	
	public void removeWriteTask(int taskid) {
//		Log.d("demo", "removeWriteTask taskid= " +taskid);
		mFileWriterList.remove(taskid);
	}
		
	public boolean addWriteTask(int taskid, int parentTaskid, String savePath, long startPos, long endPos) {
		if(mFileWriterList.get(taskid) != null) mFileWriterList.delete(taskid);
//		Log.d("demo", "addWriteTask parentTaskid = "+parentTaskid + " taskid= " +taskid);
		boolean bOk = true;
		
		FileWriter fileWriter = mFileWriterList.get(taskid);
		if(fileWriter != null) {
			fileWriter.mStartPos = startPos;
			fileWriter.mEndPos = endPos;
		} else {
			File file = new File(savePath);
			try {
				if (!file.exists()) {
					file.createNewFile();
				}		
				RandomAccessFile sFile = new RandomAccessFile(file, "rw");
				long total = getTotalLen(parentTaskid);
				if(total >0) 
					sFile.setLength(total);
				PrintStream sOuputStream = new PrintStream(new FileOutputStream(sFile.getFD()));
				fileWriter = new FileWriter(parentTaskid);
				fileWriter.mFile = sFile;
				fileWriter.mOuputStream = sOuputStream;
				fileWriter.mStartPos = startPos;
				fileWriter.mEndPos = endPos;
				mFileWriterList.append(taskid, fileWriter);
			} catch (IOException e) {
				bOk = false;
			}
		}
		return bOk;
	}

}

