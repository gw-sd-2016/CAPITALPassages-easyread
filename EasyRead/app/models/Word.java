package models;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.Expose;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
public class Word extends Model {

    private static final long serialVersionUID = 1L;

    // Fields
    @Id
    @Expose
    public Long id;

    @Required
    @Expose
    public String lemma;

    @Required
    public int length;

    @Required
    public int numSyllables;


    @Required
    public double ageOfAcquisition;

    @CreatedTimestamp
    public Timestamp createdTime;

    @UpdatedTimestamp
    public Timestamp updatedTime;

    @Required
    public boolean disavowed = false;

    @Required
    @OneToMany(cascade = CascadeType.ALL)
    public List<POS> partsOfSpeech = new ArrayList<POS>();


    private static final List<String> stopWords = Arrays.asList("a, able, about, above, according, accordingly, across, actually, after, afterwards, again, against, ain’t, all, allow, allows, almost, alone, along, already, also, although, always, am, among, amongst, an, and, another, any, anybody, anyhow, anyone, anything, anyway, anyways, anywhere, apart, appear, appreciate, appropriate, are, aren’t, around, as, aside, ask, asking, associated, at, available, away, awfully, be, became, because, become, becomes, becoming, been, before, beforehand, behind, being, believe, below, beside, besides, best, better, between, beyond, both, brief, but, by, c’mon, c’s, came, can, can’t, cannot, cant, cause, causes, certain, certainly, changes, clearly, co, com, come, comes, concerning, consequently, consider, considering, contain, containing, contains, corresponding, could, couldn’t, course, currently, definitely, described, despite, did, didn’t, different, do, does, doesn’t, doing, don’t, done, down, downwards, during, each, edu, eg, eight, either, else, elsewhere, enough, entirely, especially, et, etc, even, ever, every, everybody, everyone, everything, everywhere, ex, exactly, example, except, far, few, fifth, first, five, followed, following, follows, for, former, formerly, forth, four, from, further, furthermore, get, gets, getting, given, gives, go, goes, going, gone, got, gotten, greetings, had, hadn’t, happens, hardly, has, hasn’t, have, haven’t, having, he, he’s, hello, help, hence, her, here, here’s, hereafter, hereby, herein, hereupon, hers, herself, hi, him, himself, his, hither, hopefully, how, howbeit, however, i’d, i’ll, i’m, i’ve, ie, if, ignored, immediate, in, inasmuch, inc, indeed, indicate, indicated, indicates, inner, insofar, instead, into, inward, is, isn’t, it, it’d, it’ll, it’s, its, itself, just, keep, keeps, kept, know, knows, known, last, lately, later, latter, latterly, least, less, lest, let, let’s, like, liked, likely, little, look, looking, looks, ltd, mainly, many, may, maybe, me, mean, meanwhile, merely, might, more, moreover, most, mostly, much, must, my, myself, name, namely, nd, near, nearly, necessary, need, needs, neither, never, nevertheless, new, next, nine, no, nobody, non, none, noone, nor, normally, not, nothing, novel, now, nowhere, obviously, of, off, often, oh, ok, okay, old, on, once, one, ones, only, onto, or, other, others, otherwise, ought, our, ours, ourselves, out, outside, over, overall, own, particular, particularly, per, perhaps, placed, please, plus, possible, presumably, probably, provides, que, quite, qv, rather, rd, re, really, reasonably, regarding, regardless, regards, relatively, respectively, right, said, same, saw, say, saying, says, second, secondly, see, seeing, seem, seemed, seeming, seems, seen, self, selves, sensible, sent, serious, seriously, seven, several, shall, she, should, shouldn’t, since, six, so, some, somebody, somehow, someone, something, sometime, sometimes, somewhat, somewhere, soon, sorry, specified, specify, specifying, still, sub, such, sup, sure, t’s, take, taken, tell, tends, th, than, thank, thanks, thanx, that, that’s, thats, the, their, theirs, them, themselves, then, thence, there, there’s, thereafter, thereby, therefore, therein, theres, thereupon, these, they, they’d, they’ll, they’re, they’ve, think, third, this, thorough, thoroughly, those, though, three, through, throughout, thru, thus, to, together, too, took, toward, towards, tried, tries, truly, try, trying, twice, two, un, under, unfortunately, unless, unlikely, until, unto, up, upon, us, use, used, useful, uses, using, usually, value, various, very, via, viz, vs, want, wants, was, wasn’t, way, we, we’d, we’ll, we’re, we’ve, welcome, well, went, were, weren’t, what, what’s, whatever, when, whence, whenever, where, where’s, whereafter, whereas, whereby, wherein, whereupon, wherever, whether, which, while, whither, who, who’s, whoever, whole, whom, whose, why, will, willing, wish, with, within, without, won’t, wonder, would, would, wouldn’t, yes, yet, you, you’d, you’ll, you’re, you’ve, your, yours, yourself, yourselves, zero".split(", "));

    // Constructors
    public Word() {
    }

    public Word(String lemma, int length, int numSyllables, double ageOfAcquisition, double familiarity, boolean hasPronunciation) {
        this.lemma = lemma;
        this.length = length;
        this.numSyllables = numSyllables;
        this.ageOfAcquisition = ageOfAcquisition;
    }

    // Getters / Setters
    public static Finder<Long, Word> find = new Finder<Long, Word>(Long.class, Word.class);

    public static List<Word> all() {
        return find.where().ne("disavowed", true).findList();
    }

    public static Word create(Word word) {
        word.save();
//		word.saveManyToManyAssociations("tags");
        return word;
    }

    public static void delete(Long id) {
//		find.ref(id).delete();
        Word word = find.ref(id);
        if (word == null) {
            return;
        }

        word.disavowed = true;
        word.save();
    }


    public static boolean isStopWord(Word w) {
        return stopWords.contains(w.lemma);
    }

    public static Word byId(Long id) {
        return find.where().ne("disavowed", true).eq("id", id).findUnique();
    }

    public static Word byId(int id) {
        return find.where().ne("disavowed", true).eq("id", id).findUnique();
    }

    public static Word byRawString(String s) {
        String lc = s.toLowerCase();

        Word w;

        int x = lc.indexOf('>');


        if(x != -1){
            int y = lc.lastIndexOf("<");
            lc = lc.substring(0, lc.indexOf("<")) + lc.substring(x + 1, y);
        }

        w = Word.byLemma(lc);

        if (w == null && lc.length() - 1 > 0) w = Word.byLemma(lc.substring(0, lc.length() - 1));

        return w;
    }

    public static Word byId(String id) {
        if (id == null) {
            return null;
        }

        if (id.equals("")) {
            return null;
        }

        return byId(Long.parseLong(id));
    }

//	public static Word byWordNetId(Long wordNetId) {
//		return find.where().ne("disavowed", true).eq("wordNetId", wordNetId).findUnique();
//	}

    public static List<Word> byPartialLemma(String partialLemma, int maxResults) {
        List<Word> results = find.where()
                .ne("disavowed", true)
                .like("lemma", partialLemma + "%")
                .findList();
        int max = maxResults;
        if (results.size() < max) {
            max = results.size();
        }
        return results.subList(0, max);
    }

    public static Word byLemma(String lemma) {
        return find.where().eq("lemma", lemma).findUnique();
    }


//	public  tagString(Word word) {
//		return find.where()
//				.in("phonemeTagAppearances.word.id", id)
//				.findSet();
//	}


    /********************************
     * ENUMERATOR: For each User type
     ********************************/
    public enum Role {
        SUPERADMIN,
        ADMIN,
        INSTRUCTOR,
        STUDENT;

        public static Role getRole(String roleString) {
            Role role = null;

            switch (roleString) {
                case "Super Administrator":
                    role = SUPERADMIN;
                    break;
                case "Administrator":
                    role = ADMIN;
                    break;
                case "Instructor":
                    role = INSTRUCTOR;
                    break;
                case "Student":
                    role = STUDENT;
                    break;
            }

            return role;
        }
    }

    public static int convertAgeToGrade(double age){
        if (age <= 6) return 0;
        else if (age > 18) return 13;
        return (int) age - 6;
    }

}