package com.appmall.market.data;

import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONObject;

import android.text.TextUtils;

import com.appmall.market.bean.AppDetail;
import com.appmall.market.bean.BiBei;
import com.appmall.market.bean.CategoryList;
import com.appmall.market.bean.CheckForUpdate;
import com.appmall.market.bean.CommentList;
import com.appmall.market.bean.HotPromoteRest;
import com.appmall.market.bean.HotPromoteTop;
import com.appmall.market.bean.Keywords;
import com.appmall.market.bean.NewProd;
import com.appmall.market.bean.Rank;
import com.appmall.market.bean.SearchAssociation;
import com.appmall.market.bean.SearchRecommend;
import com.appmall.market.bean.SearchResult;
import com.appmall.market.bean.TopicDetail;
import com.appmall.market.data.DataCenter.Response;

/**
 * 数据处理
 *  
 *
 */
public class DataProcessor implements IDataConstant {

	private static final String NAME_CODE = "code";
	private static final String NAME_DATA = "data";
	private static final String NAME_CONTEXT = "context";
	private static final String NAME_ENTITIES = "entities";

	@SuppressWarnings("unchecked")
	public Response processData(int dataId, byte[] dataBytes) throws Exception {
		if (dataBytes == null)
			return null;
		
		Response resp = new Response();
		JSONObject jsonObj = new JSONObject(new String(dataBytes));
		resp.mStatusCode = jsonObj.getInt(NAME_CODE);
		if (resp.mStatusCode != IDataConstant.DATA_RESULT_OK) {
			resp.mSuccess = false;
			return resp;
		}
		
		JSONObject dataObj = jsonObj.optJSONObject(NAME_DATA);
		if (dataObj != null) {
			JSONObject entities = dataObj.optJSONObject(NAME_ENTITIES);
			if (entities != null) {
				resp.mData = buildData(dataId, entities);
			}
			
			resp.mContext = new HashMap<String, String>();
			JSONObject contextObj = dataObj.optJSONObject(NAME_CONTEXT);
			if (contextObj != null) {
				Iterator<String> iterator = contextObj.keys();
				while (iterator.hasNext()) {
					String key = iterator.next();
					String value = contextObj.getString(key);
					if (TextUtils.isEmpty(key))
						continue;
					resp.mContext.put(key, value);
				}
			}
			resp.mOtherData = buildOtherData(dataId, dataObj);
		}
		resp.mSuccess = true;
		
		return resp;
	}

	protected IDataBase buildData(int dataId, JSONObject jsonObj) throws Exception {
		switch (dataId) {
		case HOMEPAGE:
			HotPromoteTop top = new HotPromoteTop();
			top.readFromJSON(jsonObj);
			return top;
		case HOMEPAGE_MORE:
			HotPromoteRest rest = new HotPromoteRest();
			rest.readFromJSON(jsonObj);
			return rest;
		case MUST_INSTALL_APP:
		case MUST_INSTALL_GAME:
			BiBei bibei = new BiBei();
			bibei.readFromJSON(jsonObj);
			return bibei;
		case APP_CATEGORY:
		case GAME_CATEGORY:
			CategoryList cl = new CategoryList();
			cl.readFromJSON(jsonObj);
			return cl;
		case APP_RANK:
		case GAME_RANK:
		case CATEGORY_DETAIL_HOT:
			Rank rank = new Rank();
			rank.readFromJSON(jsonObj);
			return rank;
		case APP_NEW_PROD:
		case GAME_NEW_PROD:
		case CATEGORY_DETAIL_NEWPROD:
			NewProd newProd = new NewProd();
			newProd.readFromJSON(jsonObj);
			return newProd;
		case SEARCH_TAGS:
			Keywords tags = new Keywords();
			tags.readFromJSON(jsonObj);
			return tags;
		case SEARCH_RESULT:
			SearchResult result = new SearchResult();
			result.readFromJSON(jsonObj);
			return result;
		case SEARCH_ASSOCIATION:
			SearchAssociation association = new SearchAssociation();
			association.readFromJSON(jsonObj);
			return association;
		case TOPIC_DETAIL:
			TopicDetail td = new TopicDetail();
			td.readFromJSON(jsonObj);
			return td;
		case APP_DETAIL:
			AppDetail ad = new AppDetail();
			ad.readFromJSON(jsonObj);
			return ad;
		case CHECK_UPDATE:
			if (jsonObj.length() <= 0)
				return null;
			
			CheckForUpdate update = new CheckForUpdate();
			update.readFromJSON(jsonObj);
			return update;
		case APP_DETAIL_COMMENT:
			CommentList commList = new CommentList();
			commList.readFromJSON(jsonObj);
			return commList;
		default:
			return null;
		}
	}
	
	private IDataBase buildOtherData(int dataId, JSONObject jsonObj) throws Exception {
		switch (dataId) {
		case SEARCH_RESULT:
			SearchRecommend sr = new SearchRecommend();
			sr.readFromJSON(jsonObj);
			return sr;
		default:
			return null;
		}
	}
	
}
