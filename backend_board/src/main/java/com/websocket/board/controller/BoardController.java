package com.websocket.board.controller;

import com.websocket.board.model.SocketBoardMessage;
import com.websocket.board.repo.ChannelRedisRepository;
import com.websocket.board.repo.ChannelRepository;
import com.websocket.board.repo.SocketBoardMessageRepository;
import com.websocket.board.service.BoardClientService;
import com.websocket.board.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.javers.core.Changes;
import org.javers.core.Javers;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.javers.repository.jql.QueryBuilder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/board")
public class BoardController {

    private final ChannelRedisRepository channelRedisRepository;
    private final SocketBoardMessageRepository socketBoardMessageRepository;
    private final ChannelRepository channelRepository;
    private final ChannelService channelService;
    private final BoardClientService boardClientService;
    private final Javers javers;

    @GetMapping("/{channelId}/history")
    @ResponseBody
    public void getPersonChanges(@PathVariable("channelId") String channelId) {
        QueryBuilder jqlQuery = QueryBuilder.byInstanceId(channelId, SocketBoardMessage.class)
                .withNewObjectChanges();

        List<CdoSnapshot> snapshots = javers.findSnapshots(jqlQuery.build());
//        System.out.println(changes.prettyPrint());
        for (CdoSnapshot cs: snapshots) {
            System.out.println(cs.toString());
        }
    }

    @CrossOrigin("*")
    @GetMapping("/{channelId}")
    public SocketBoardMessage getBoardStatusInit(
            @RequestHeader(name = "Authorization") String Authorization,
            @PathVariable("channelId") String channelId) {

        SocketBoardMessage socketBoardMessage = channelRedisRepository.findBoardByChannelId(channelId);

        if (boardClientService.checkToken(Authorization).getIsValid()) {
            socketBoardMessage.setMemberList(channelService.getChannelMember(channelId));
        }

        // 레디스에 해당 채널의 정보가 없다면
        // 디비에서 채널 정보를 쿼리해와서
        // 레디스에 올려주기
        if(socketBoardMessage == null) {
            System.out.println("레디스에 해당 채널에 대한 정보 없음");
            System.out.println("DB에서 Redis로 보드 상태 올리기");
            Optional<SocketBoardMessage> boardStatus = socketBoardMessageRepository.findById(channelId);
            if(boardStatus.isPresent()) {
                // 클라이언트에 전송할 보드 상태 세팅
                socketBoardMessage = boardStatus.get();
                // 레디스에 올려줄 보드 상태 세팅
                String channelName = channelRepository.findByChannelId(channelId).get().getChannelName();
                channelRedisRepository.createChannel(channelName, channelId);
                channelRedisRepository.updateBoard(socketBoardMessage);
            }
        }

        return socketBoardMessage;
    }

    @CrossOrigin("*")
    @GetMapping("/tutorial/{channelId}")
    public SocketBoardMessage getBoardStatusInit(
            @PathVariable("channelId") String channelId) {

        SocketBoardMessage socketBoardMessage = channelRedisRepository.findBoardByChannelId(channelId);

        if (socketBoardMessage == null) {
            System.out.println("레디스에 해당 채널에 대한 정보 없음");
            System.out.println("DB에서 Redis로 보드 상태 올리기");
            Optional<SocketBoardMessage> boardStatus = socketBoardMessageRepository.findById(channelId);
            if(boardStatus.isPresent()) {
                // 클라이언트에 전송할 보드 상태 세팅
                socketBoardMessage = boardStatus.get();
                // 레디스에 올려줄 보드 상태 세팅
                String channelName = channelRepository.findByChannelId(channelId).get().getChannelName();
                channelRedisRepository.createChannel(channelName, channelId);
                channelRedisRepository.updateBoard(socketBoardMessage);

//                List<Channel> list = channelRedisRepository.findAllChannel();
//                System.out.println(list.toString());
            } else {
                channelRedisRepository.createChannel("Tutorial Channel s2", "earlyBird10TeamTestChannel1");
                socketBoardMessage = channelRedisRepository.findBoardByChannelId(channelId);
            }
        }

        if (channelId.equals("earlyBird10TeamTestChannel1")) {
            List<String> tutorialMockMember = new ArrayList<>();
            tutorialMockMember.add("이정훈");
            tutorialMockMember.add("정용우");
            tutorialMockMember.add("김강현");
            tutorialMockMember.add("최문경");
            tutorialMockMember.add("정진권");

            socketBoardMessage.setMemberList(tutorialMockMember);
        }

        return socketBoardMessage;
    }



}
