package reactive.demo.reactive.controller;

import lombok.*;
import reactive.demo.reactive.enums.MessageType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage {

    private String content;
    private String sender;
    private MessageType messageType;
}
