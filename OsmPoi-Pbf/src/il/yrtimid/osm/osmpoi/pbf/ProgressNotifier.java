package il.yrtimid.osm.osmpoi.pbf;

public interface ProgressNotifier {
	public void onProgressChange(Progress newProgress);
	
	public class Progress{
		private Long count;
		private String message;
		private Long maximum;
		
		public Progress(Long count){
			this.count = count;
		}

		public Progress(Long count, Long maximum){
			this.count = count;
			this.maximum = maximum;
		}

		public Progress(Long count, String message){
			this.count = count;
			this.message = message;
		}

		public Progress(Long count, Long maximum, String message){
			this.count = count;
			this.maximum = maximum;
			this.message = message;
		}

		public Progress(String message){
			this.message = message;
		}
		
		public Long getCount(){return count;}
		public String getMessage(){return message;}
		public Long getMaximum(){return maximum;}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder b = new StringBuilder();
			if (count != null) b.append(count);
			if (maximum != null) b.append("/").append(maximum);
			if (message != null) b.append("; ").append(message);
			return b.toString();
		}
		
	}
}
