/**
 *  Copyright (c) 2016, Carnegie Mellon University.  All Rights Reserved.
 */

import java.io.*;

/**
 *  The OR operator for all retrieval models.
 */
public class QrySopAnd extends QrySop {

/**
 *  Indicates whether the query has a match.
 *  @param r The retrieval model that determines what is a match
 *  @return True if the query matches, otherwise false.
 */
public boolean docIteratorHasMatch (RetrievalModel r) {
        return this.docIteratorHasMatchAll (r);
}

/**
 *  Get a score for the document that docIteratorHasMatch matched.
 *  @param r The retrieval model that determines how scores are calculated.
 *  @return The document score.
 *  @throws IOException Error accessing the Lucene index
 */
public double getScore (RetrievalModel r) throws IOException {

        if (r instanceof RetrievalModelUnrankedBoolean) {
                return this.getScoreUnrankedBoolean (r);
        } else if (r instanceof RetrievalModelRankedBoolean) {
                return this.getScoreRankedBoolean(r);
        }
        else{
                throw new IllegalArgumentException
                              (r.getClass().getName() + " doesn't support the OR operator.");
        }
}

/**
 *  getScore for the UnrankedBoolean retrieval model.
 *  @param r The retrieval model that determines how scores are calculated.
 *  @return The document score.
 *  @throws IOException Error accessing the Lucene index
 */
private double getScoreUnrankedBoolean (RetrievalModel r) throws IOException {
        int doc_id=this.docIteratorGetMatch();
        for (Qry qry:this.args) {
                if (!qry.docIteratorHasMatch(r) || qry.docIteratorGetMatch()!=doc_id)
                        return 0.0;
        }
        return 1.0;


}

/** score is the MIN(tf) in the document
 *  getScore for the RankedBoolean retrieval model.
 *  @param r The retrieval model that determines how scores are calculated.
 *  @return The document score.
 *  @throws IOException Error accessing the Lucene index
 */
private double getScoreRankedBoolean (RetrievalModel r) throws IOException {

        // min_score of the same document for different args
        int doc_id = this.docIteratorGetMatch();
        double min_score=Double.MAX_VALUE;
        for (Qry qry:this.args) {
                if (!qry.docIteratorHasMatch(r) || qry.docIteratorGetMatch()!=doc_id) {
                        return 0.0;
                }
                double score = (double)((QrySop) qry).getScore(r);
                if (score<min_score)
                        min_score=score;
        }

        return min_score;
}

}