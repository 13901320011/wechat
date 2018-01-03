package com.wechat.controller;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.wechat.entity.msg.LocationMsg;
import com.wechat.entity.msg.PicMsg;
import com.wechat.entity.msg.TextMsg;
import com.wechat.service.EventService;
import com.wechat.service.MsgService;
import com.wechat.util.CheckoutUtil;
import com.wechat.util.XmlUtil;

/**
 * 
 * @author sky
 * 
 * 此controller主要是与微信服务器通信，接管公众平台发来的所有消息
 *
 */
@Controller
public class WeChatController {
	/**
	 * 处理微信服务器发来的get请求，进行签名的验证
	 * @param signature 微信端发来的签名
	 * @param timestamp 微信端发来的时间戳
	 * @param nonce 微信端发来的随机字符串
	 * @param echostr 微信端发来的验证字符串
	 */
	@RequestMapping(value = "wechat.do", method = RequestMethod.GET)
	public void validate(HttpServletRequest req, HttpServletResponse response,
			@RequestParam(value = "signature") String signature,
			@RequestParam(value = "timestamp") String timestamp,
			@RequestParam(value = "nonce") String nonce,
			@RequestParam(value = "echostr") String echostr) {
		if (CheckoutUtil.checkSignature(signature, timestamp, nonce)) {
            try {
            	PrintWriter   print = response.getWriter();
                print.write(echostr);
                print.flush();
                print.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
	}
	
	/**
	 * 此处是处理微信服务器的消息转发的
	 * @param req
	 * @param resp
	 * @throws JAXBException
	 */

	@RequestMapping(value = "wechat.do", method = RequestMethod.POST)
	public void processMsg(HttpServletRequest req, HttpServletResponse resp)
			throws JAXBException {
		try {
			resp.setContentType("text/xml;charset=utf-8");

			PrintWriter out = resp.getWriter();
			String requestContent = ConvertToString(req.getInputStream());
			//判断消息类型，以处理不同类型的消息
			String msgType = MsgService.checkMsgType(requestContent);
			System.out.println("msgType:" + msgType + "\n");
			String xml = "";
			if (msgType.equals("text")) {
				TextMsg msg = (TextMsg) XmlUtil.xmlToObject(requestContent,
						TextMsg.class);
				System.out.println(msg.getContent());
				xml = MsgService.processTextMsg(msg);

				System.out.println(xml + "\n");

			} else if (msgType.equals("image")) {
				PicMsg msg = (PicMsg) XmlUtil.xmlToObject(requestContent,
						PicMsg.class);
				xml = MsgService.processImageMsg(msg);
				System.out.println(xml + "\n");

			} else if (msgType.equals("location")) {
				LocationMsg msg = (LocationMsg) XmlUtil.xmlToObject(
						requestContent, LocationMsg.class);
			} else if (msgType.equals("event")) {
				xml = EventService.distributeEvent(requestContent);
				System.out.println(xml + "\n");

			}
			if (xml != "") {
				out.write(xml);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	//处理响应回来的内容
	public String ConvertToString(InputStream is) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is,
				"utf-8"));
		StringBuilder strBuilder = new StringBuilder();
		String strRead = "";
		while ((strRead = reader.readLine()) != null) {
			strBuilder.append(strRead);
		}
		return strBuilder.toString();
	}

	@RequestMapping(value = "gettoken.do")
	public void getToken() {
		
	}
	
}