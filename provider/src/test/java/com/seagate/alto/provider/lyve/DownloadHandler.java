/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.lyve;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

class DownloadHandler implements Callback<ResponseBody> {
    private final String path;
    private final int[] count;

    public DownloadHandler(String path, final int[] count) {
        this.path = path;
        this.count = count;
    }

    @Override
    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        InputStream in = null;
        ByteArrayOutputStream out = null;
        long t = System.currentTimeMillis();
        try {
            in = new BufferedInputStream(response.body().byteStream());
            out = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int cnt = 0;
            while ((cnt = in.read(buff, 0, buff.length)) != -1) {
                out.write(buff, 0, cnt);
            }
            out.flush();
            System.out.println(path + ": " + out.size() + " bytes, " + (System.currentTimeMillis() - t) + "ms");
            count[1] += out.size();
        } catch (Exception e) {
            System.out.println(path + ": failed in " + (System.currentTimeMillis() - t) + "ms");
            //e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            count[0]--;
        }
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        t.printStackTrace();
    }

}
