package com.offershow.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.offershow.exception.BusinessException;
import com.offershow.exception.ResourceNotFoundException;
import com.offershow.model.dto.OfferDTO;
import com.offershow.model.entity.Offer;
import com.offershow.model.vo.OfferVO;
import com.offershow.model.vo.PageVO;
import com.offershow.repository.OfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Offer 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OfferServiceImpl implements OfferService {

    private final OfferRepository offerRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public OfferVO createOffer(OfferDTO offerDTO) {
        try {
            Offer offer = new Offer();
            BeanUtils.copyProperties(offerDTO, offer);

            // 转换薪资结构为JSON字符串
            offer.setSalaryStructure(objectMapper.writeValueAsString(offerDTO.getSalaryStructure()));

            // 设置创建时间和更新时间
            LocalDateTime now = LocalDateTime.now();
            offer.setCreatedAt(now);
            offer.setUpdatedAt(now);

            // 保存Offer
            offerRepository.insert(offer);

            return convertToVO(offer);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert salary structure to JSON", e);
            throw new BusinessException("薪资结构格式不正确");
        }
    }

    @Override
    @Transactional
    public OfferVO updateOffer(Long id, OfferDTO offerDTO) {
        Offer existingOffer = offerRepository.findById(id);
        if (existingOffer == null || Boolean.TRUE.equals(existingOffer.getIsDeleted())) {
            throw new ResourceNotFoundException("Offer不存在: " + id);
        }

        try {
            BeanUtils.copyProperties(offerDTO, existingOffer);

            // 转换薪资结构为JSON字符串
            existingOffer.setSalaryStructure(objectMapper.writeValueAsString(offerDTO.getSalaryStructure()));

            // 更新时间
            existingOffer.setUpdatedAt(LocalDateTime.now());

            // 更新Offer
            offerRepository.update(existingOffer);

            return convertToVO(existingOffer);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert salary structure to JSON", e);
            throw new BusinessException("薪资结构格式不正确");
        }
    }

    @Override
    @Transactional
    public OfferVO patchOffer(Long id, OfferDTO offerDTO) {
        Offer existingOffer = offerRepository.findById(id);
        if (existingOffer == null || Boolean.TRUE.equals(existingOffer.getIsDeleted())) {
            throw new ResourceNotFoundException("Offer不存在: " + id);
        }

        try {
            // 只更新非null字段
            if (offerDTO.getCompanyName() != null) {
                existingOffer.setCompanyName(offerDTO.getCompanyName());
            }
            if (offerDTO.getPosition() != null) {
                existingOffer.setPosition(offerDTO.getPosition());
            }
            if (offerDTO.getCity() != null) {
                existingOffer.setCity(offerDTO.getCity());
            }
            if (offerDTO.getSalaryStructure() != null) {
                existingOffer.setSalaryStructure(objectMapper.writeValueAsString(offerDTO.getSalaryStructure()));
            }
            if (offerDTO.getWorkYears() != null) {
                existingOffer.setWorkYears(offerDTO.getWorkYears());
            }
            if (offerDTO.getJobDescription() != null) {
                existingOffer.setJobDescription(offerDTO.getJobDescription());
            }
            if (offerDTO.getInterviewProcess() != null) {
                existingOffer.setInterviewProcess(offerDTO.getInterviewProcess());
            }
            if (offerDTO.getInterviewDifficulty() != null) {
                existingOffer.setInterviewDifficulty(offerDTO.getInterviewDifficulty());
            }
            if (offerDTO.getIsAccepted() != null) {
                existingOffer.setIsAccepted(offerDTO.getIsAccepted());
            }
            if (offerDTO.getRejectReason() != null) {
                existingOffer.setRejectReason(offerDTO.getRejectReason());
            }

            // 更新时间
            existingOffer.setUpdatedAt(LocalDateTime.now());

            // 更新Offer
            offerRepository.update(existingOffer);

            return convertToVO(existingOffer);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert salary structure to JSON", e);
            throw new BusinessException("薪资结构格式不正确");
        }
    }

    @Override
    public OfferVO getOfferById(Long id) {
        Offer offer = offerRepository.findById(id);
        if (offer == null || Boolean.TRUE.equals(offer.getIsDeleted())) {
            throw new ResourceNotFoundException("Offer不存在: " + id);
        }

        return convertToVO(offer);
    }

    @Override
    @Transactional
    public boolean deleteOffer(Long id) {
        Offer offer = offerRepository.findById(id);
        if (offer == null || Boolean.TRUE.equals(offer.getIsDeleted())) {
            throw new ResourceNotFoundException("Offer不存在: " + id);
        }

        // 逻辑删除
        offer.setIsDeleted(true);
        offer.setUpdatedAt(LocalDateTime.now());
        offerRepository.update(offer);

        return true;
    }

    @Override
    public PageVO<OfferVO> searchOffers(String keyword, int page, int size) {
        // 获取总记录数
        int total = offerRepository.countByKeyword(keyword);

        // 计算总页数
        int pages = (total + size - 1) / size;

        // 查询数据
        List<Offer> offers = offerRepository.findByKeyword(keyword, (page - 1) * size, size);

        // 转换为VO
        List<OfferVO> offerVOs = offers.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 构建分页结果
        return PageVO.<OfferVO>builder()
                .total(total)
                .pages(pages)
                .current(page)
                .size(size)
                .records(offerVOs)
                .build();
    }

    @Override
    @Transactional
    public List<OfferVO> batchCreateOffers(List<OfferDTO> offerDTOs) {
        List<OfferVO> results = new ArrayList<>();

        for (OfferDTO offerDTO : offerDTOs) {
            try {
                OfferVO offerVO = createOffer(offerDTO);
                results.add(offerVO);
            } catch (Exception e) {
                log.error("Failed to create offer: {}", offerDTO, e);
                // 继续处理下一个，不中断批处理
            }
        }

        return results;
    }

    @Override
    @Transactional
    public List<Long> batchDeleteOffers(List<Long> ids) {
        List<Long> successIds = new ArrayList<>();

        for (Long id : ids) {
            try {
                boolean success = deleteOffer(id);
                if (success) {
                    successIds.add(id);
                }
            } catch (Exception e) {
                log.error("Failed to delete offer: {}", id, e);
                // 继续处理下一个，不中断批处理
            }
        }

        return successIds;
    }

    /**
     * 将实体对象转换为视图对象
     *
     * @param offer Offer实体
     * @return OfferVO视图对象
     */
    private OfferVO convertToVO(Offer offer) {
        OfferVO offerVO = new OfferVO();
        BeanUtils.copyProperties(offer, offerVO);

        try {
            // 转换JSON字符串为对象
            if (offer.getSalaryStructure() != null) {
                offerVO.setSalaryStructure(objectMapper.readValue(offer.getSalaryStructure(), Object.class));
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to parse salary structure", e);
            offerVO.setSalaryStructure(null);
        }

        return offerVO;
    }
}