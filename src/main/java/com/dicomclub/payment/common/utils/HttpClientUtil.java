package com.dicomclub.payment.common.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

@Component
public class HttpClientUtil {

	private static final Log logeer = LogFactory.getLog(HttpClientUtil.class);


	public static HttpResponse postJson(String path, String json,String  charset) {
		HttpClient httpClient = null;
		HttpPost httpPost = null;
		String result = null;
		try {
			httpClient = HttpClientBuilder.create().build();
			httpPost = new HttpPost(path.replaceAll("(?<!:)/{2,}", "/"));
			StringEntity entity = new StringEntity(json,charset);
			entity.setContentType("application/json");
			httpPost.setEntity(entity);
			HttpResponse response = httpClient.execute(httpPost);
			return response;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}


	public static String doGet(String url, Map<String, Object> map, String charset, Header[] headers) {
		if(map!=null&&map.size()>0){
			url = url+ "?" + mapToUrlStr(map);
		}
		logeer .info("*****url:  "+url);
		String result = "";
		try {
			// 根据地址获取请求
			HttpGet request = new HttpGet(url.replaceAll("(?<!:)/{2,}", "/"));// 这里发送get请求
			if(headers!=null){
				for(Header header :headers){
					request.addHeader(header);
				}
			}

			// 获取当前客户端对象
			HttpClient httpClient = HttpClientBuilder.create().build();



			// 通过请求对象获取响应对象
			HttpResponse response = httpClient.execute(request);

			// 判断网络连接状态码是否正常(0--200都数正常)
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				result = EntityUtils.toString(response.getEntity(), charset);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		logeer .info("*****result:  "+result);
		return result;
	}

	public static String doPost(String url, String str, String charset,  Header[] headers) {
		HttpClient httpClient = null;
		HttpPost httpPost = null;
		String result = null;
		try {
			httpClient = HttpClientBuilder.create().build();
			httpPost = new HttpPost(url.replaceAll("(?<!:)/{2,}", "/"));
			StringEntity entity = new StringEntity(str,charset);
			entity.setContentType("application/json");
			httpPost.setEntity(entity);
			if( headers!=null){
				for(Header header :headers){
					httpPost.addHeader(header);
				}
			}
			HttpResponse response = httpClient.execute(httpPost);
			if (response != null) {
				HttpEntity resEntity = response.getEntity();
				if (resEntity != null) {
					result = EntityUtils.toString(resEntity, charset);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}


	public static String doPost(String url, String str, String charset) {
		HttpClient httpClient = null;
		HttpPost httpPost = null;
		String result = null;
		try {
			httpClient = HttpClientBuilder.create().build();
			httpPost = new HttpPost(url.replaceAll("(?<!:)/{2,}", "/"));
			StringEntity entity = new StringEntity(str,charset);
			entity.setContentType("application/json");
			httpPost.setEntity(entity);
			HttpResponse response = httpClient.execute(httpPost);
			if (response != null) {
				HttpEntity resEntity = response.getEntity();
				if (resEntity != null) {
					result = EntityUtils.toString(resEntity, charset);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}

	/**
	 * get请求
	 * @param url
	 * @param map
	 * @param charset
	 * @return
	 */
	public static String doGet(String url, Map<String, Object> map, String charset) {
		url = url+ "?" + mapToUrlStr(map);
		logeer .info("*****url:  "+url);
		String result = "";
		try {
			// 根据地址获取请求
			HttpGet request = new HttpGet(url.replaceAll("(?<!:)/{2,}", "/"));// 这里发送get请求
			// 获取当前客户端对象
			HttpClient httpClient = HttpClientBuilder.create().build();
			// 通过请求对象获取响应对象
			HttpResponse response = httpClient.execute(request);

			// 判断网络连接状态码是否正常(0--200都数正常)
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				result = EntityUtils.toString(response.getEntity(), charset);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		logeer .info("*****result:  "+result);
		return result;
	}

	private static String mapToUrlStr(Map<String, Object> map){
		StringBuilder stringBuilder = new StringBuilder();
		Set<String> keys = map.keySet();
		for (String string : keys) {
			if ( map.get(string)!=null) {
				Object value = map.get(string);
//                if (value.contains("[")) {
//                    JSONArray array = JSON.parseArray(value);
//                    array.forEach(index -> {
//                        stringBuilder.append("&" + string + "[]" + "=" + index);
//                    });
//                }
				if (value instanceof Collection) {
					Collection array = (Collection) value;
					array.forEach(index -> {
						stringBuilder.append("&" + string   + "=" + index);
					});
				}else{
					stringBuilder.append("&" + string + "=" + value);
				}
			}
		}
		if (stringBuilder.toString().length() > 0) {
			return stringBuilder.toString().substring(1);
		}
		return stringBuilder.toString();
	}



	/**
	 * 判断object是否为基本类型(不为对象)
	 *
	 * @param object
	 * @return
	 */
	public static boolean isBaseType(Object object) {
		Class className = object.getClass();
		if (className.equals(java.lang.Integer.class) ||
				className.equals(java.lang.Byte.class) ||
				className.equals(java.lang.Long.class) ||
				className.equals(java.lang.Double.class) ||
				className.equals(java.lang.Float.class) ||
				className.equals(java.lang.Character.class) ||
				className.equals(java.lang.String.class) ||
				className.equals(java.lang.Short.class) ||
				className.equals(java.lang.Boolean.class)) {
			return true;
		}
		return false;
	}

	/**
	 * @param fileName 图片路径
	 */
//	public static String uploadFileWithHttpMime(String url,String fileName) {
//		// 实例化http客户端
//		HttpClient httpClient = HttpClients.custom().build();
//		// 实例化post提交方式
//		HttpPost post = new HttpPost(url);
//
//		// 添加json参数
//		try {
//			// 设置上传文件
//			fileName = fileName.replaceAll("\\\\","/");
//			if(fileName.indexOf(ResourceUtil.getDjsaasFileDir()) == -1){
//				fileName = ResourceUtil.getDjsaasFileDir() + fileName;
//			}
//			File file = new File(fileName.replace(".pdf",""));
//			// 文件参数内容
//			FileBody fileBody = new FileBody(file);
//			// 添加文件参数
//			//实例化参数对象
//			fileName = fileName.replace(ResourceUtil.getDjsaasFileDir(),"");
//			HttpEntity params = MultipartEntityBuilder.create()
//					.addPart("file", fileBody)
//					.addPart("path", new StringBody(fileName))
//					.addPart("fileName", new StringBody(file.getName())).build();
//			// 将参数加入post请求体中
//			post.setEntity(params);
//			// 执行post请求并得到返回对象 [ 到这一步我们的请求就开始了 ]
//			HttpResponse resp = httpClient.execute(post);
//			// 解析返回请求结果
//			HttpEntity entity = resp.getEntity();
//			InputStream is = entity.getContent();
//			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
//			StringBuffer buffer = new StringBuffer();
//			String temp;
//			while ((temp = reader.readLine()) != null) {
//				buffer.append(temp);
//			}
////			System.out.println(buffer);
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		} catch (ClientProtocolException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (IllegalStateException e) {
//			e.printStackTrace();
//		}
//		return fileName;
//	}

	/**
	 * 网上获取文件
	 *
	 * @param savepath 保存路径
	 * @param resurl  资源路径
	 * @param fileName  自定义资源名
	 * @return false:文件已存在 true：文件获取成功
	 */
	public static boolean getInternetRes(String savepath, String resurl, String fileName) {
		URL url = null;
		HttpURLConnection con = null;
		InputStream in = null;
		FileOutputStream out = null;
		try {
			url = new URL(resurl);
			//建立http连接，得到连接对象
			con = (HttpURLConnection) url.openConnection();
			//con.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
			in = con.getInputStream();
			byte[] data = getByteData(in);//转化为byte数组

			File file = new File(savepath);
			if (!file.exists()) {
				file.mkdirs();
			}

			File res = new File(file + File.separator + fileName);
			if (res.exists()) {
				return false;
			}
			out = new FileOutputStream(res);
			out.write(data);
			logeer .info("downloaded successfully!");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != out) {
					out.close();
				}
				if (null != in) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return true;
	}

	public static InputStream getInputStreamFromUrl(String resurl){
		URL url = null;
		HttpURLConnection con = null;
		InputStream in = null;
		FileOutputStream out = null;

		try {
			url = new URL(resurl);
			con = (HttpURLConnection) url.openConnection();
			return con.getInputStream();
		} catch (Exception e) {
			throw new RuntimeException("资源加载错误");
		}
	}

	/**
	 * 从输入流中获取字节数组
	 *
	 * @param in
	 * @return
	 * @throws IOException
	 */
	private static byte[] getByteData(InputStream in) throws IOException {
		byte[] b = new byte[1024];
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int len = 0;
		while ((len = in.read(b)) != -1) {
			bos.write(b, 0, len);
		}
		if(null!=bos){
			bos.close();
		}
		return bos.toByteArray();
	}


    public static HttpResponse get(String url, Map<String,Object> map, String charset) {

        url = url+ "?" + mapToUrlStr(map);
        logeer .info("*****url:  "+url);
        String result = "";
        try {
            // 根据地址获取请求
            HttpGet request = new HttpGet(url.replaceAll("(?<!:)/{2,}", "/"));// 这里发送get请求
            // 获取当前客户端对象
            HttpClient httpClient = HttpClientBuilder.create().build();
            // 通过请求对象获取响应对象
            HttpResponse response = httpClient.execute(request);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return  null;
        }

    }
}
