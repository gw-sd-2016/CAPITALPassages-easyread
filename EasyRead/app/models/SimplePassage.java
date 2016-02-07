package models;


import com.avaje.ebean.annotation.Expose;
import controllers.PassageAnalysisController;
import formdata.SimplePassageData;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.*;


@Entity
public class SimplePassage extends Model {

    private static final long serialVersionUID = 1L;

    private static final List<String> stopWords = Arrays.asList("a’s, able, about, above, according, accordingly, across, actually, after, afterwards, again, against, ain’t, all, allow, allows, almost, alone, along, already, also, although, always, am, among, amongst, an, and, another, any, anybody, anyhow, anyone, anything, anyway, anyways, anywhere, apart, appear, appreciate, appropriate, are, aren’t, around, as, aside, ask, asking, associated, at, available, away, awfully, be, became, because, become, becomes, becoming, been, before, beforehand, behind, being, believe, below, beside, besides, best, better, between, beyond, both, brief, but, by, c’mon, c’s, came, can, can’t, cannot, cant, cause, causes, certain, certainly, changes, clearly, co, com, come, comes, concerning, consequently, consider, considering, contain, containing, contains, corresponding, could, couldn’t, course, currently, definitely, described, despite, did, didn’t, different, do, does, doesn’t, doing, don’t, done, down, downwards, during, each, edu, eg, eight, either, else, elsewhere, enough, entirely, especially, et, etc, even, ever, every, everybody, everyone, everything, everywhere, ex, exactly, example, except, far, few, fifth, first, five, followed, following, follows, for, former, formerly, forth, four, from, further, furthermore, get, gets, getting, given, gives, go, goes, going, gone, got, gotten, greetings, had, hadn’t, happens, hardly, has, hasn’t, have, haven’t, having, he, he’s, hello, help, hence, her, here, here’s, hereafter, hereby, herein, hereupon, hers, herself, hi, him, himself, his, hither, hopefully, how, howbeit, however, i’d, i’ll, i’m, i’ve, ie, if, ignored, immediate, in, inasmuch, inc, indeed, indicate, indicated, indicates, inner, insofar, instead, into, inward, is, isn’t, it, it’d, it’ll, it’s, its, itself, just, keep, keeps, kept, know, knows, known, last, lately, later, latter, latterly, least, less, lest, let, let’s, like, liked, likely, little, look, looking, looks, ltd, mainly, many, may, maybe, me, mean, meanwhile, merely, might, more, moreover, most, mostly, much, must, my, myself, name, namely, nd, near, nearly, necessary, need, needs, neither, never, nevertheless, new, next, nine, no, nobody, non, none, noone, nor, normally, not, nothing, novel, now, nowhere, obviously, of, off, often, oh, ok, okay, old, on, once, one, ones, only, onto, or, other, others, otherwise, ought, our, ours, ourselves, out, outside, over, overall, own, particular, particularly, per, perhaps, placed, please, plus, possible, presumably, probably, provides, que, quite, qv, rather, rd, re, really, reasonably, regarding, regardless, regards, relatively, respectively, right, said, same, saw, say, saying, says, second, secondly, see, seeing, seem, seemed, seeming, seems, seen, self, selves, sensible, sent, serious, seriously, seven, several, shall, she, should, shouldn’t, since, six, so, some, somebody, somehow, someone, something, sometime, sometimes, somewhat, somewhere, soon, sorry, specified, specify, specifying, still, sub, such, sup, sure, t’s, take, taken, tell, tends, th, than, thank, thanks, thanx, that, that’s, thats, the, their, theirs, them, themselves, then, thence, there, there’s, thereafter, thereby, therefore, therein, theres, thereupon, these, they, they’d, they’ll, they’re, they’ve, think, third, this, thorough, thoroughly, those, though, three, through, throughout, thru, thus, to, together, too, took, toward, towards, tried, tries, truly, try, trying, twice, two, un, under, unfortunately, unless, unlikely, until, unto, up, upon, us, use, used, useful, uses, using, usually, value, various, very, via, viz, vs, want, wants, was, wasn’t, way, we, we’d, we’ll, we’re, we’ve, welcome, well, went, were, weren’t, what, what’s, whatever, when, whence, whenever, where, where’s, whereafter, whereas, whereby, wherein, whereupon, wherever, whether, which, while, whither, who, who’s, whoever, whole, whom, whose, why, will, willing, wish, with, within, without, won’t, wonder, would, would, wouldn’t, yes, yet, you, you’d, you’ll, you’re, you’ve, your, yours, yourself, yourselves, zero".split(", "));
    // Fields
    @Id
    @Expose
    public Long id;

    @Required
    @Expose
    public String title;

    //https://groups.google.com/forum/#!topic/play-framework/jkrw2wh48uc
    @Required
    @Lob
    public String text;


    @Required
    public String source;

    @Required
    public int grade;

    @Required
    @Expose
    public long instructorID;

    @OneToMany(cascade = CascadeType.ALL)
    public List<Sentence> sentences = new ArrayList<Sentence>();

    @OneToMany(cascade = CascadeType.ALL)
    public Set<PassageQuestion> questions = new HashSet<PassageQuestion>();

    @Expose
    public Long tagId;

    @Expose
    @ManyToMany(cascade = CascadeType.ALL)
    public Set<PassageTag> tags = new HashSet<PassageTag>();

    @Expose
    public int num_characters;

    @Expose
    public int numSyllables;

    @Expose
    public int numWords;


    public List<String> getStopWords() {
        return stopWords;
    }


    public static Finder<String, SimplePassage> find = new Finder<String, SimplePassage>(String.class, SimplePassage.class);

    public static SimplePassage byId(Long id) {
        return find.where().eq("id", id).findUnique();
    }

    public static List<SimplePassage> byInstructorId(Long id) {
        return find.where().eq("instructor_id", id).findList();
    }

    public static List<SimplePassage> bySource(String source) {
        return find.where().eq("source", source).findList();
    }

    public static List<SimplePassage> byGrade(int grade) {
        return find.where().eq("grade", grade).findList();
    }

    public static List<SimplePassage> byPassageTagName(String name) {
        List<SimplePassage> res = new ArrayList<SimplePassage>();
        for (SimplePassage p : all()) {
            for (PassageTag t : p.tags) {
                if (t.name.equals(name)) res.add(p);
            }
        }
        return res;
    }

    public static List<SimplePassage> all() {
        return find.where().findList();
    }

    public SimplePassage(String p, String t, String s) {
        this.text = p;
        this.title = t;
        this.source = s;
    }

    public SimplePassage(SimplePassageData data) {
        this.text = data.getPassageText();
        this.title = data.getPassageTitle();
        this.source = data.getPassageSource();
    }

    public static List<SimplePassage> allPassages() {
        return find.all();
    }

    public static SimplePassage create(SimplePassage passage) {
        passage.save();
        return passage;
    }

    public static void acceptWord(String word, int grade) {
        Word thisWord = Word.byLemma(word);
        if (thisWord != null) {
            thisWord.ageOfAcquisition = grade + 6;
            thisWord.save();
        }
    }






    public List<String> sentenceBreakdown() {
        PassageAnalysisController analysisController = new PassageAnalysisController();

        List<String> difficulties = new ArrayList<String>();

        if (this.sentences.size() > 2) {
            for (int i = 2; i < this.sentences.size(); i++) {
                if (i % 2 == 0) {
                    difficulties.add(i + " " + analysisController.determineGradeLevelForString(this.sentences.get(i - 1).text + " " + this.sentences.get(i).text));
                }
            }
        }

        return difficulties;
    }
}