package ua.kiev.prog.bot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.InputStream;

public enum BotState {

    Start {
        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Hello!");
            sendPhoto(context, "hi.png");
        }

        @Override
        public BotState nextState() {
            return EnterPhone;
        }
    },

    EnterPhone {
        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Enter your phone number please:");
        }

        @Override
        public void handleInput(BotContext context) {
            context.getUser().setPhone(context.getInput());
        }

        @Override
        public BotState nextState() {
            return EnterEmail;
        }
    },

    EnterEmail {
        private BotState next;

        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Enter your e-mail please:");
        }

        @Override
        public void handleInput(BotContext context) {
            String email = context.getInput();

            if (Utils.isValidEmailAddress(email)) {
                context.getUser().setEmail(context.getInput());
                next = Approved;
            } else {
                sendMessage(context, "Wrong e-mail address!");
                next = EnterEmail;
            }
        }

        @Override
        public BotState nextState() {
            return next;
        }
    },

    Approved(false) {
        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Thank you for application!");
        }

        @Override
        public BotState nextState() {
            return Start;
        }
    };

    // --------------- //

    private static BotState[] states;
    private final boolean inputNeeded;

    BotState() {
        this.inputNeeded = true;
    }

    BotState(boolean inputNeeded) {
        this.inputNeeded = inputNeeded;
    }

    public static BotState getInitialState() {
        return byId(0);
    }

    public static BotState byId(int id) {
        if (states == null) {
            states = BotState.values();
        }

        return states[id];
    }

    protected void sendMessage(BotContext context, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(Long.toString(context.getUser().getChatId()));
        message.setText(text);
        try {
            context.getBot().execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    //метод для відправлення картинки при першому зверненні до чат-боту
    protected void sendPhoto(BotContext context, String namePhoto) {
        InputStream is = getClass().getClassLoader()
                .getResourceAsStream(namePhoto);

        SendPhoto message = new SendPhoto();
        message.setChatId(Long.toString(context.getUser().getChatId()));
        message.setPhoto(new InputFile(is, parsingPhotoName(namePhoto)));
        try {
            context.getBot().execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    //метод для розбору назви фото
    public String parsingPhotoName(String namePhoto) {
        int n = namePhoto.indexOf(".");
        String name = namePhoto.substring(0, n);
        return name;
    }

    public boolean isInputNeeded() {
        return inputNeeded;
    }

    public void handleInput(BotContext context) {
        // do nothing by default
    }

    public abstract void enter(BotContext context);

    public abstract BotState nextState();
}
