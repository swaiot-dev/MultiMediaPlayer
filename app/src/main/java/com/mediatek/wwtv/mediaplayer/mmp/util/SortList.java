package com.mediatek.wwtv.mediaplayer.mmp.util;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.mediatek.wwtv.mediaplayer.mmp.model.FileAdapter;
import com.mediatek.wwtv.util.MtkLog;

public class SortList{

    public void sort(List<FileAdapter> list,final String method,final String method2){
        Collections.sort(list,new Comparator(){

            @Override
            public int compare(Object a, Object b) {
                // TODO Auto-generated method stub
                int ret = 0;

                try{
//                  Method ma=((T)a).getClass().getMethod(method, null);
//                  Method mb=((T)b).getClass().getMethod(method, null);
//                  String resulta = (String)ma.invoke(((T)a), null);
//                  String resultb = (String)mb.invoke(((T)b), null);
                    String resulta= null;
                    String resultb = null;
                    if(method.equals("getName")){
                        resulta = ((FileAdapter)(a)).getName();
                        resultb = ((FileAdapter)(b)).getName();
                    }else if(method.equals("getSuffix")){
                        resulta = ((FileAdapter)(a)).getSuffix();
                        resultb = ((FileAdapter)(b)).getSuffix();
                    }else if(method.equals("Date")){
                        resulta = ((FileAdapter)(a)).getLastModified();
                        resultb = ((FileAdapter)(b)).getLastModified();
                    }
                    if(null  == resulta ||  null == resultb){
                        if(resulta!=null){
                            return resulta.length();
                        }
                        if(resultb!=null){
                            return resultb.length();
                        }
                        return -1;
                    }

                    if(resulta!=null){
                        ret = resulta.toLowerCase().compareTo(resultb.toLowerCase());
                    }
//                    MtkLog.i("sort", "resulta:"+resulta+"  resultb:"+resultb);
                }catch(Exception e){
                    e.printStackTrace();
                }
                return ret;
            }

        });
    }
}
