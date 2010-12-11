
package mathmu.data;

import java.util.Date;

/**
 *
 * @author XiaoR
 */
public class Task {
    private String exp;
    private Long id;
    private Long owner=new Long(-1);

    public Task(String exp){
        this.exp = exp;
        id = new Date().getTime();
    }
    
    public Task(String exp, Long ownerid){
    	this.exp=exp;
    	id = new Date().getTime();
    	owner=ownerid;
    }

    public Task(){

    }
    
    public Long getOwner(){
    	return owner;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public String getExp() {
        return exp;
    }

    public void setExp(String exp) {
        this.exp = exp;
    }

    @Override
    public boolean equals(Object o){
        return this.id == ((Task) o).getId();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
    
    public void selfIntro(){
    	System.out.println("hello, everyone, my owner is "+owner+", and my id is "+id+" , the exp is "+exp);
    }
}
