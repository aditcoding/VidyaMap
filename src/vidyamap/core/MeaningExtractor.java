package vidyamap.core;

import vidyamap.parser.SearchRequest;

public interface MeaningExtractor {
	
	public String extractMeaning(SearchRequest req);
	
}