/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.io.Serializable;

/**
 *   A list of postings for a given word.
 */
public class PostingsList implements Serializable {
	/** The postings list as a linked list. */
	private LinkedList<PostingsEntry> list = new LinkedList<PostingsEntry>();

	/**  Number of postings in this list  */
	public int size() {
		return list.size();
	}

	public PostingsEntry get(int i) {
		return list.get(i);
	}

	public boolean add(int docID, int offset) {
		// Allow multiple occurences in same document,
		// use unique() to remove duplicates when presenting.
		//return list.add(entry);

		try {
			PostingsEntry last = list.getLast();
			if (last.docID == docID)
				return last.addOffset(offset);
		} catch (NoSuchElementException e) {}

		return list.add(new PostingsEntry(docID, offset));
	}

	public boolean add(PostingsEntry entry) {
		return list.add(entry);
	}

	/**
	 * Returns a new instance with duplicate docID occurences removed.
	 * Only necessary if add() allows multiple occurences in same document.
	 */
	public PostingsList unique() {
		PostingsList uniq = new PostingsList();
		PostingsEntry prev = null;

		for (PostingsEntry entry : list) {
			if (prev == null || prev.docID != entry.docID)
				uniq.add(entry);
			prev = entry;
		}

		return uniq;
	}

	public PostingsList unionWith(PostingsList other) {
		ListIterator<PostingsEntry> it1 = list.listIterator();
		ListIterator<PostingsEntry> it2 = other.list.listIterator();

		PostingsList answer = new PostingsList();

		try {
			PostingsEntry p1 = it1.next();
			PostingsEntry p2 = it2.next();
			while (p1 != null && p2 != null) {
				if (p1.docID == p2.docID) {
					// Not a real union, but works in this case
					answer.add(p1);
					p1 = it1.next();
					p2 = it2.next();
				} else if (p1.docID < p2.docID) {
					answer.add(p1);
					p1 = it1.next();
				} else {
					answer.add(p2);
					p2 = it2.next();
				}
			}
		} catch (NoSuchElementException e) {}

		while (it1.hasNext())
			answer.add(it1.next());

		while (it2.hasNext())
			answer.add(it2.next());

		return answer;
	}

	public PostingsList intersect(PostingsList other) {
		return intersect(other, false);
	}

	public PostingsList intersect(PostingsList other, boolean phrase) {
		PostingsList answer = new PostingsList();

		ListIterator<PostingsEntry> it1 = list.listIterator();
		ListIterator<PostingsEntry> it2 = other.list.listIterator();

		try {
			PostingsEntry p1 = it1.next();
			PostingsEntry p2 = it2.next();

			while (true) {
				if (p1.docID == p2.docID) {
					if (phrase) { // Phrase searching
						ListIterator<Integer> pit1 = p1.offsets.listIterator();
						ListIterator<Integer> pit2 = p2.offsets.listIterator();
						while (pit1.hasNext()) {
							int pp1 = pit1.next();
							while (pit2.hasNext()) {
								int pp2 = pit2.next();
								if (pp2 - pp1 == 1) // adjacent words (w1 followed by w2)
									answer.add(p1);
								else if (pp2 > pp1) // w2 comes to late after w1
									break;
							}
							// while (!l.isempty && l.peek != null & l.peek - p1 == 1)
						}
					} else { // Normal intersect
						answer.add(p1);
					}
					p1 = it1.next();
					p2 = it2.next();
				} else if (p1.docID < p2.docID) {
					p1 = it1.next();
				} else {
					p2 = it2.next();
				}
			}
		} catch (NoSuchElementException e) {}

		return answer;
	}
}
