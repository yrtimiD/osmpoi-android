package il.yrtimid.osm.osmpoi.pbf;

import il.yrtimid.osm.osmpoi.ItemPipe;
import il.yrtimid.osm.osmpoi.domain.*;
import java.io.InputStream;
import crosby.binary.file.BlockInputStream;
import crosby.binary.osmosis.OsmosisBinaryParser;


public class OsmImporter {
	
	public static Long countEntities(InputStream input, ProgressNotifier progressNotifier){
		Long result = 0L;
		
		try {
			OsmosisBinaryParser parser = new OsmosisBinaryParser();

			PeriodicProgressNotifier counter = new PeriodicProgressNotifier(5000, progressNotifier);
			
			parser.setSink(counter);
			counter.setSink(new NullSink());
			
			BlockInputStream stream = new BlockInputStream(input, parser);
			stream.process();
			stream.close();
			
			result = counter.getCount();
		}catch(Exception ioe){
			ioe.printStackTrace();
			result = -1L;
        }
		
		return result;
	}
	
	public static Long processAll(InputStream input, ItemPipe<Entity> newItemNotifier, ProgressNotifier progressNotifier){
		Long result = 0L;
		
		try {
			OsmosisBinaryParser parser = new OsmosisBinaryParser();
			PeriodicProgressNotifier counter = new PeriodicProgressNotifier(5000, progressNotifier);
			EntityHandler handler = new EntityHandler(newItemNotifier);

			parser.setSink(counter);
			counter.setSink(handler);
			
			BlockInputStream stream = new BlockInputStream(input, parser);
			stream.process();
			stream.close();
			
			result = counter.getCount();
		}catch(Exception ioe){
			ioe.printStackTrace();
			result = -1L;
        }
		
		return result;
	}

}
