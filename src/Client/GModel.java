package Client;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Observable;

/**
 * The model is used for three things:
 * Geting questions from the .txt file
 * Writing questions to the .txt file
 * Comparing if the right answer has been pressed
 * 
 * @author Danijel
 */
public class GModel extends Observable {

	private Options options;
	private String rightAnswer;
	private String chosenAnswer;
	private SingleQuestion activeQuestion;
	private int rightCount = 0;
	private int wrongCount = 0;
	private int qNumber = 0;
	private ArrayList<SingleQuestion> qu;
	private boolean isCorrect = false;
	private QuestionsClient q;
	private Sounds sounds;

	public GModel() {
		try {
			q = new QuestionsClient();
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			setChanged();
			notifyObservers(3);
		}
		sounds = new Sounds();
	}

	public void changeActiveQuestion() {
		if(!qu.equals(null)) {
			if (qNumber >= options.getGameRounds()) {
				int[] a = { rightCount, wrongCount };
				setChanged();
				notifyObservers(a);
				qNumber = 0;
				wrongCount = 0;
				rightCount = 0;
			}

			else {
				activeQuestion = qu.get(qNumber);
				setRight(activeQuestion.getCorrectAnswer());
				setChanged();
				notifyObservers(activeQuestion);
			}
		}
	}
	/**
	 * Initiates a game round with the submitted settings
	 * @param options
	 */
	public void playGame(Options options) {
		this.options = options;
		try {
			ConnectionToServer c = new ConnectionToServer();
			qu = c.getQuestions(options.getGameRounds());

		} catch (IOException e) {
			setChanged();
			notifyObservers(1);
			try {
				qu = q.getQuestions(options.getGameRounds());
			} catch (Throwable e1) {
				e1.printStackTrace();
				setChanged();
				notifyObservers(3);
				System.exit(0);
			}
		}
		sounds.onOff(options.getVolume());
	}
	/**
	 * Stores the answer chosen by the user.
	 * @param chosenAnswer
	 */
	public void setChosenAnswer(String chosenAnswer) {
		this.chosenAnswer = chosenAnswer;
	}

	/**
	 * Stores the right answer in a separate variable.
	 * @param rightAnswer
	 */
	public void setRight(String rightAnswer) {
		this.rightAnswer = rightAnswer;
	}
	public String getRight() {
		return rightAnswer;
	}
	/**
	 * This method does the comparing
	 * of the pressed answer with the 
	 * correct answer
	 */
	public void isRightAnswer() {
		if (rightAnswer.equals(chosenAnswer)) {
			sounds.playCorrect();
			rightCount++;
			isCorrect = true;
		} else {
			wrongCount++;
			sounds.playIncorrect();
			isCorrect = false;
		}
		qNumber++;
		setChanged();
		notifyObservers(chosenAnswer);
		setChanged();
		notifyObservers(isCorrect);
	}
	/**
	 * the method for writing a question
	 * @param qu
	 */
	public void createQuestion(SingleQuestion qu) {
		if(qu != null){
			/**
			 * Tries to connect to server, if not possible writes to local disk
			 */
			try {
				ConnectionToServer c = new ConnectionToServer();
				c.writeQuestion(qu);	

			} catch (IOException e) {
				setChanged();
				notifyObservers(2);						//Error code for not being able to write to the server
				q.writeQuestion(qu);
			}
		}
	} 
}
