package com.ll.netmong.domain.park.service;

import com.ll.netmong.base.config.ApiKeys;
import com.ll.netmong.domain.likePark.repository.LikedParkRepository;
import com.ll.netmong.domain.member.entity.Member;
import com.ll.netmong.domain.member.repository.MemberRepository;
import com.ll.netmong.domain.park.dto.response.ParkResponse;
import com.ll.netmong.domain.park.entity.Park;
import com.ll.netmong.domain.park.repository.ParkRepository;
import com.ll.netmong.domain.postComment.exception.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParkServiceImpl implements ParkService {

    private final ParkRepository parkRepository;
    private final ApiKeys apikeys;
    private final LikedParkRepository likedParkRepository;
    private final MemberRepository memberRepository;

// 실제 배포 시 활성화
//    @PostConstruct
//    public void init() {
//        if (parkRepository.count() == 0) {
//            saveParksFromApi();
//        }
//    }

    @Override
    @Transactional(readOnly = true)
    public List<ParkResponse> getParks() {
        List<Park> parks = parkRepository.findAll();
        return convertToParkResponses(parks);
    }

    @Transactional(readOnly = true)
    public List<Park> fetchParksFromApi() {
        int pageNo = 1;
        List<Park> parks = new ArrayList<>();

        while (true) {
            String result = callApi(pageNo);
            List<Park> newParks = parseParksData(result);
            if (newParks.isEmpty()) {
                break;
            }
            parks.addAll(newParks);
            pageNo++;
        }

        return parks;
    }

    @Transactional
    public void saveParksToDatabase(List<Park> parks) {
        parkRepository.saveAll(parks);
    }

    @Override
    public void saveParksFromApi() {
        List<Park> parks = fetchParksFromApi();
        saveParksToDatabase(parks);
    }

    @Override
    @Transactional(readOnly = true)
    public ParkResponse getPark(Long parkId, UserDetails userDetails) {
        Park park = parkRepository.findById(parkId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 공원이 존재하지 않습니다: " + parkId));

        Member member = memberRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new DataNotFoundException("사용자를 찾을 수 없습니다."));

        boolean isLiked = likedParkRepository.existsByMemberAndPark(member, park);

        return park.toResponse(isLiked);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getStates() {
        return parkRepository.findStates();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getCitiesByState(String state) {
        return parkRepository.findCitiesByState(state);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParkResponse> getParksByStateAndCity(String state, String city, UserDetails userDetails) {
        List<Park> parks = parkRepository.findByLnmadrStartingWith(state + " " + city);

        Member member = memberRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new DataNotFoundException("사용자를 찾을 수 없습니다."));

        List<Long> likedParkIds = likedParkRepository.findLikedParkIdsByMemberId(member.getId());

        List<ParkResponse> parkResponses = parks.stream().map(park -> {
            boolean isLiked = likedParkIds.contains(park.getId());
            return park.toResponse(isLiked);
        }).collect(Collectors.toList());

        return parkResponses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParkResponse> getParksWithPetAllowed() {
        List<Park> parks = parkRepository.findByPetAllowedTrue();
        return parks.stream()
                .map(park -> park.toResponse(null))
                .collect(Collectors.toList());
    }

    private List<ParkResponse> convertToParkResponses(List<Park> parks) {
        return parks.stream()
                .map(park -> park.toResponse(null))
                .collect(Collectors.toList());
    }

    private String callApi(int pageNo) {
        String urlStr = "http://api.data.go.kr/openapi/tn_pubr_public_cty_park_info_api" +
                "?ServiceKey=" + apikeys.getParkApiKey() +
                "&pageNo=" + pageNo +
                "&numOfRows=100";
        String result;
        try {
            URL url = new URL(urlStr);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");

            BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));

            String returnLine;
            StringBuilder resultBuilder = new StringBuilder();

            while ((returnLine = br.readLine()) != null) {
                resultBuilder.append(returnLine).append("\n\r");
            }

            urlConnection.disconnect();
            result = resultBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }

    private List<Park> parseParksData(String data) {
        List<Park> parks = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(data)));

            NodeList nodeList = document.getElementsByTagName("item");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                parks.add(createParkFromElement(element));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parks;
    }

    private Park createParkFromElement(Element element) {
        String parkNm = element.getElementsByTagName("parkNm").item(0).getTextContent();
        String lnmadr = element.getElementsByTagName("lnmadr").item(0).getTextContent();
        String latitudeStr = element.getElementsByTagName("latitude").item(0).getTextContent();
        double latitude = latitudeStr.isEmpty() ? 0 : Double.parseDouble(latitudeStr);
        String longitudeStr = element.getElementsByTagName("longitude").item(0).getTextContent();
        double longitude = longitudeStr.isEmpty() ? 0 : Double.parseDouble(longitudeStr);
        String phoneNumber = element.getElementsByTagName("phoneNumber").item(0).getTextContent();
        String[] lnmadrSplit = lnmadr.split("\\s");
        String state = lnmadrSplit.length > 0 ? lnmadrSplit[0] : "";
        String city = lnmadrSplit.length > 1 ? lnmadrSplit[1] : "";

        return Park.builder()
                .parkNm(parkNm)
                .lnmadr(lnmadr)
                .latitude(latitude)
                .longitude(longitude)
                .phoneNumber(phoneNumber)
                .state(state)
                .city(city)
                .build();
    }
}
