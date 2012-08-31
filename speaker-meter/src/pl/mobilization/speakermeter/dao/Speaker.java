package pl.mobilization.speakermeter.dao;

import java.io.Serializable;

import com.google.common.base.Strings;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table SPEAKER.
 */
public class Speaker implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -3853772691270679584L;
	private Long id;
    /** Not-null value. */
    private String name;
    /** Not-null value. */
    private String presentation;
    private int votes_up;
    private int votes_down;

    public Speaker() {
    }

    public Speaker(Long id) {
        this.id = id;
    }

    public Speaker(Long id, String name, String presentation, int votes_up, int votes_down) {
        this(id);
        this.name = name;
        this.presentation = presentation;
        this.votes_up = votes_up;
        this.votes_down = votes_down;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** Not-null value. */
    public String getName() {
        return name;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setName(String name) {
        this.name = name;
    }

    /** Not-null value. */
    public String getPresentation() {
        return Strings.nullToEmpty(presentation);
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setPresentation(String presentation) {
        this.presentation = presentation;
    }

    public int getVotesUp() {
        return votes_up;
    }

    public void setVotesUp(int votes_up) {
        this.votes_up = votes_up;
    }

    public int getVotesDown() {
        return votes_down;
    }

    public void setVotesDown(int votes_down) {
        this.votes_down = votes_down;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Speaker other = (Speaker) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}