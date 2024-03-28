package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class PaginationService {

    public <T> List<T> getPaginatedList(List<T> allItems, int limit, int offset) {
        int totalItems = allItems.size();
        int endIndex = Math.min(offset + limit, totalItems);

        if (offset >= totalItems || offset < 0) {
            return Collections.emptyList();
        }

        endIndex = Math.min(endIndex, totalItems);

        return allItems.subList(offset, endIndex);
    }
}