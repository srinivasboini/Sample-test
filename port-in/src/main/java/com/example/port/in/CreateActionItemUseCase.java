package com.example.port.in;

import com.example.domain.model.ActionItem;

public interface CreateActionItemUseCase {
    ActionItem createActionItem(CreateActionItemCommand command);
}
