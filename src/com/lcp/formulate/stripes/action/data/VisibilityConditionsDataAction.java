package com.lcp.formulate.stripes.action.data;

import java.sql.SQLException;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.Validate;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.lcp.formulate.entities.EntityManager;
import com.lcp.formulate.entities.JsonViews;
import com.lcp.formulate.entities.ormlite.ViewField;
import com.lcp.formulate.entities.ormlite.VisibilityCondition;
import com.lcp.formulate.stripes.action.BaseActionBean;
import com.lcp.formulate.stripes.exceptions.AjaxException;

@UrlBinding("/data/visibility/{viewField}")
public class VisibilityConditionsDataAction extends BaseActionBean implements DataAction {
	private static Logger log = org.apache.log4j.Logger.getLogger(VisibilityConditionsDataAction.class);

	@Validate(required=true)
	private ViewField viewField;
	public void setViewField(ViewField viewField) { this.viewField = viewField; }
	public ViewField getViewField() { return viewField; }
	
	private ConnectionSource conn;	
	
	@DefaultHandler
	public Resolution defaultResolution() throws AjaxException {
		log.info("VisibilityConditionsDataAction");
		try {
			conn = EntityManager.getConnection();
			Dao<VisibilityCondition, String> dao = DaoManager.createDao(conn, VisibilityCondition.class);
			
			String string = new ObjectMapper().configure(Feature.DEFAULT_VIEW_INCLUSION, false)
						.writerWithView(JsonViews.VisibilityConditions.class)
							.writeValueAsString( dao.queryForEq("viewfields_id", getViewField().getId()) );
		
			log.info(string);
			return new StreamingResolution("text/json",string);
		} catch (Exception e) {
			e.printStackTrace();
			getContext().getValidationErrors().add("entity", new SimpleError("unknown exception ["+e.getMessage()+"]"));
			throw new AjaxException(this, "visibilityConditions", "data error");
		} finally {
			try { conn.close(); } catch (SQLException e) {}
		}
	}
}
