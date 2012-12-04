// This software is released into the Public Domain.  See copying.txt for details.
package crosby.binary.osmosis;

import il.yrtimid.osm.osmpoi.domain.*;
import il.yrtimid.osm.osmpoi.osmosis.core.task.v0_6.Sink;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openstreetmap.osmosis.core.OsmosisConstants;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;



import crosby.binary.BinaryParser;
import crosby.binary.Osmformat;
import crosby.binary.Osmformat.DenseInfo;

/** Class that reads and parses binary files and sends the contained entities to the sink. */
public class OsmosisBinaryParser extends BinaryParser {

    @Override
    public void complete() {
        sink.complete();
        sink.release();
    }

    /** The magic number used to indicate no version number metadata for this entity. */
    static final int NOVERSION = -1;
    /** The magic number used to indicate no changeset metadata for this entity. */
    static final int NOCHANGESET = -1;

    @Override
    protected void parseNodes(List<Osmformat.Node> nodes) {
      for (Osmformat.Node i : nodes) {
        List<Tag> tags = new ArrayList<Tag>();
        for (int j = 0; j < i.getKeysCount(); j++) {
          tags.add(new Tag(getStringById(i.getKeys(j)), getStringById(i.getVals(j))));
        }
        // long id, int version, Date timestamp, OsmUser user,
        // long changesetId, Collection<Tag> tags,
        // double latitude, double longitude
        Node tmp;
        long id = i.getId();
        double latf = parseLat(i.getLat()), lonf = parseLon(i.getLon());

        if (i.hasInfo()) {
          Osmformat.Info info = i.getInfo();
          tmp = new Node(new CommonEntityData(id, getDate(info), tags), latf, lonf);
        } else {
          tmp = new Node(new CommonEntityData(id, NODATE, tags), latf, lonf);
        }
        sink.process(tmp);

      }
    }
    
    @Override
    protected void parseDense(Osmformat.DenseNodes nodes) {
        long lastId = 0, lastLat = 0, lastLon = 0;
        
        int j = 0; // Index into the keysvals array.

        // Stuff for dense info
        long lasttimestamp = 0, lastchangeset = 0;
        int lastuserSid = 0, lastuid = 0;
        DenseInfo di = null;
        if (nodes.hasDenseinfo()) {
          di = nodes.getDenseinfo();
        }
        for (int i = 0; i < nodes.getIdCount(); i++) {
            Node tmp;
            List<Tag> tags = new ArrayList<Tag>(0);
            long lat = nodes.getLat(i) + lastLat;
            lastLat = lat;
            long lon = nodes.getLon(i) + lastLon;
            lastLon = lon;
            long id = nodes.getId(i) + lastId;
            lastId = id;
            double latf = parseLat(lat), lonf = parseLon(lon);
            // If empty, assume that nothing here has keys or vals.
            if (nodes.getKeysValsCount() > 0) {
                while (nodes.getKeysVals(j) != 0) {
                    int keyid = nodes.getKeysVals(j++);
                    int valid = nodes.getKeysVals(j++);
                    tags.add(new Tag(getStringById(keyid), getStringById(valid)));
                }
                j++; // Skip over the '0' delimiter.
            }
            // Handle dense info.
            if (di != null) {
              int uid = di.getUid(i) + lastuid; lastuid = uid;
              int userSid = di.getUserSid(i) + lastuserSid; lastuserSid = userSid;
              long timestamp = di.getTimestamp(i) + lasttimestamp; lasttimestamp = timestamp;
	          @SuppressWarnings("unused")
	          int version = di.getVersion(i); 
              long changeset = di.getChangeset(i) + lastchangeset; lastchangeset = changeset;

              Date date = new Date(date_granularity * timestamp);

              tmp = new Node(new CommonEntityData(id, date, tags), latf, lonf);
            } else {
                tmp = new Node(new CommonEntityData(id, NODATE, tags), latf, lonf);
            }
            sink.process(tmp);
        }
    }

    @Override
    protected void parseWays(List<Osmformat.Way> ways) {
        for (Osmformat.Way i : ways) {
            List<Tag> tags = new ArrayList<Tag>();
            for (int j = 0; j < i.getKeysCount(); j++) {
                tags.add(new Tag(getStringById(i.getKeys(j)), getStringById(i.getVals(j))));
            }
                
            long lastId = 0;
            List<Node> nodes = new ArrayList<Node>();
            for (long j : i.getRefsList()) {
                nodes.add(new Node(j + lastId));
                lastId = j + lastId;
            }

            long id = i.getId();

            // long id, int version, Date timestamp, OsmUser user,
            // long changesetId, Collection<Tag> tags,
            // List<WayNode> wayNodes
            Way tmp;
            if (i.hasInfo()) {
                Osmformat.Info info = i.getInfo();
                tmp = new Way(new CommonEntityData(id, getDate(info), tags), nodes);
            } else {
                tmp = new Way(new CommonEntityData(id, NODATE, tags), nodes);
            }
            sink.process(tmp);
        }
    }

    @Override
    protected void parseRelations(List<Osmformat.Relation> rels) {
        for (Osmformat.Relation i : rels) {
            List<Tag> tags = new ArrayList<Tag>();
            for (int j = 0; j < i.getKeysCount(); j++) {
                tags.add(new Tag(getStringById(i.getKeys(j)), getStringById(i.getVals(j))));
            }

            long id = i.getId();

            long lastMid = 0;
            List<RelationMember> nodes = new ArrayList<RelationMember>();
            for (int j = 0; j < i.getMemidsCount(); j++) {
                long mid = lastMid + i.getMemids(j);
                lastMid = mid;
                String role = getStringById(i.getRolesSid(j));
                EntityType etype = null;

                if (i.getTypes(j) == Osmformat.Relation.MemberType.NODE) {
                    etype = EntityType.Node;
                } else if (i.getTypes(j) == Osmformat.Relation.MemberType.WAY) {
                    etype = EntityType.Way;
                } else if (i.getTypes(j) == Osmformat.Relation.MemberType.RELATION) {
                    etype = EntityType.Relation;
                } else {
                    assert false; // TODO; Illegal file?
                }

                nodes.add(new RelationMember(mid, etype, role));
            }
            // long id, int version, TimestampContainer timestampContainer,
            // OsmUser user,
            // long changesetId, Collection<Tag> tags,
            // List<RelationMember> members
            Relation tmp;
            if (i.hasInfo()) {
                Osmformat.Info info = i.getInfo();
                tmp = new Relation(new CommonEntityData(id, getDate(info), tags), nodes);
            } else {
                tmp = new Relation(new CommonEntityData(id, NODATE, tags), nodes);
            }
            sink.process(tmp);
        }
    }

    @Override
    public void parse(Osmformat.HeaderBlock block) {
        for (String s : block.getRequiredFeaturesList()) {
            if (s.equals("OsmSchema-V0.6")) {
              continue; // We can parse this.
            }
            if (s.equals("DenseNodes")) {
              continue; // We can parse this.
            }
           throw new OsmosisRuntimeException("File requires unknown feature: " + s);
        }
        
        if (block.hasBbox()) {
            String source = OsmosisConstants.VERSION;
            if (block.hasSource()) {
                source = block.getSource();
            }

            double multiplier = .000000001;
            double rightf = block.getBbox().getRight() * multiplier;
            double leftf = block.getBbox().getLeft() * multiplier;
            double topf = block.getBbox().getTop() * multiplier;
            double bottomf = block.getBbox().getBottom() * multiplier;

            Bound bounds = new Bound(rightf, leftf, topf, bottomf, source);
            sink.process(bounds);
        }
    }

    
    /**
     * {@inheritDoc}
     */
    public void setSink(Sink sink) {
       this.sink = sink;
    }

    private Sink sink;
}
