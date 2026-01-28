package MasterCut.domain.utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Message
{
	private static Message instance;
	private List<MessageHandler> subscribers = new ArrayList<>();

	// Singleton pour éviter de passer l'objet partout
	public static Message getInstance() {
		if (instance == null) {
			instance = new Message();
		}
		return instance;
	}

	// Ajouter un abonné
	public void subscribe(MessageHandler handler) {
		subscribers.add(handler);
	}

	// Envoyer un message à tous les abonnés
	public void publish(String message, Color color) {
		for (MessageHandler handler : subscribers) {
			handler.displayMessage(message, color);
		}
	}

	public static void sendMessage(String message, Color color) {
		getInstance().publish(message, color);
	}

	public static void sendMessage(String message) {
		getInstance().publish(message, Color.BLACK);
	}
}
