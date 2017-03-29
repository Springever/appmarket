package com.appmall.market.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;
import com.appmall.market.R;

public class UninstallUtility {
	
	public static String m_binPath = null;
	
	public static void notifyUninstall(Context ctx) {
        assertBinaries(ctx);
        startBin(ctx);
	}
	
	private static boolean assertBinaries(Context ctx) {
		try {
				File file = new File(ctx.getDir("bin",0), "uninstallnotify");
				if (!file.exists()) {
					copyRawFile(ctx, R.raw.uninstallnotify, file, "755");
				}
				m_binPath = file.getAbsolutePath();
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}
	
	private static void copyRawFile(Context ctx, int resid, File file, String mode) throws IOException, InterruptedException
	{
		final String abspath = file.getAbsolutePath();
		final FileOutputStream out = new FileOutputStream(file);
		final InputStream is = ctx.getResources().openRawResource(resid);
		byte buf[] = new byte[1024];
		int len;
		while ((len = is.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		out.close();
		is.close();
		
		Runtime.getRuntime().exec("chmod "+mode+" "+abspath).waitFor();
		
	}
	
	@SuppressWarnings("deprecation")
	private static boolean startBin(Context ctx){
		try{
			Process p = Runtime.getRuntime().exec("ps"); 
			p.waitFor();
	        BufferedReader in = new BufferedReader(   
                       new InputStreamReader(p.getInputStream()));
	        String line = in.readLine();
	        boolean found = false;
	        do{
	        	if(line.contains("uninstallnotify")){
	        		found = true;
	        		break;
	        	}
	        	line = in.readLine();
	        }while(line != null);
			in.close();
			if(found)
				return false;
		
		}catch (Exception e){
			return false;
		}
		int sysVersion = Integer.parseInt(VERSION.SDK); 
		String Serial = null;
		if(sysVersion >= 17  )
			Serial = getUserSerial(ctx);
		
		try{
			if(Serial == null){	
				Runtime.getRuntime().exec(m_binPath);
			}
			else{
				String cmd = m_binPath;
				cmd += " ";
				cmd += Serial;
				Runtime.getRuntime().exec(cmd);
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return true;
	}
	
	private static String getUserSerial(Context context)
    {
		if(Build.VERSION.SDK_INT < 17)
			return null; 
        Object userManager = context.getSystemService("user");
        if (userManager == null)
        {
            return null;
        }
        
        try
        {
            Method myUserHandleMethod = android.os.Process.class.getMethod("myUserHandle", (Class<?>[]) null);
            Object myUserHandle = myUserHandleMethod.invoke(android.os.Process.class, (Object[]) null);
            
            Method getSerialNumberForUser = userManager.getClass().getMethod("getSerialNumberForUser", myUserHandle.getClass());
            long userSerial = (Long) getSerialNumberForUser.invoke(userManager, myUserHandle);
            return String.valueOf(userSerial);
        }
        catch (Exception e) {
        	
        }     
        return null;
    }
}