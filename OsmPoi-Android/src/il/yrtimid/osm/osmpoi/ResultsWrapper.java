///**
// * 
// */
//package il.yrtimid.osm.osmpoi;
//
//import java.util.ArrayList;
//import java.util.Collection;
//
//import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
//import org.openstreetmap.osmosis.core.domain.v0_6.Node;
//
//import il.yrtimid.osm.osmpoi.UI.Action;
//import il.yrtimid.osm.osmpoi.UI.ResultsAdapter;
//
///**
// * @author yrtimid
// *
// */
//public class ResultsWrapper {
//	ResultsAdapter adapter;
//	boolean cancel = false;
//	private Action onProgress;
//	
//	public ResultsWrapper(ResultsAdapter adapter){
//		this.adapter = adapter;
//	}
//	
//	public void setOnProgress(Action action){
//		this.onProgress = action;
//	}
//	
//	public void setCancel(boolean cancel){
//		this.cancel = cancel;
//	}
//	
//	public boolean isCancel(){
//		return this.cancel;
//	}
//	
//	public void addItem(Entity entity){
//		adapter.addItem(entity);
//		if (onProgress!= null)
//			onProgress.onAction();
//	}
//	
//	public void addEntities(Collection<Entity> entities){
//		adapter.addItems(entities);
//		if (onProgress!= null)
//			onProgress.onAction();
//	}
//	
//	public void addNodes(Collection<Node> nodes){
//		Collection<Entity> entities = new ArrayList<Entity>(nodes);
//		addEntities(entities);
//	}
//	
//	public void update(){
//		adapter.update();
//	}
//
//
//}
