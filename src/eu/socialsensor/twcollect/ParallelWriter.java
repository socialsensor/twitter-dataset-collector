package eu.socialsensor.twcollect;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 
 * @author kleinmind
 * The code was adapted from the original in:
 * http://stackoverflow.com/questions/6206472/what-is-the-best-way-to-write-to-a-file-in-a-parallel-thread-in-java
 */
public class ParallelWriter implements Runnable {

    private File file;
    private boolean resume;
    private BlockingQueue<Item> q;

    public ParallelWriter(File f, boolean resume){
        this.file = f;
        this.resume = resume;
        this.q = new LinkedBlockingQueue<Item>();
    }

    public ParallelWriter append( CharSequence str ){
        try {
            CharSeqItem item = new CharSeqItem();
            item.content = str;
            item.type = ItemType.CHARSEQ;
            q.put(item);
            return this;
        } catch (InterruptedException ex) {
            throw new RuntimeException( ex );
        }
    }

    public void end(){
        try {
            Item item = new Item();
            item.type = ItemType.POISON;
            q.put(item);
        } catch (InterruptedException ex) {
            throw new RuntimeException( ex );
        }
    }

    public void run() {

        BufferedWriter out = null;
        Item item = null;

        try{
            out = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(file, resume),"UTF-8") );
            while( (item = q.take()).type != ItemType.POISON ){
                out.append( ((CharSeqItem)item).content );
                out.newLine();
            }
        } catch (InterruptedException ex){
            throw new RuntimeException( ex );
        } catch  (IOException ex) {
            throw new RuntimeException( ex );
        } finally {
            if( out != null ) try {
                out.close();
            } catch (IOException ex) {
                throw new RuntimeException( ex );
            }
        }
    }

    private enum ItemType {
        CHARSEQ, POISON;
    }
    private static class Item {
        ItemType type;
    }
    private static class CharSeqItem extends Item {
        CharSequence content;
    }
 
}