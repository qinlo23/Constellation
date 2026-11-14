package com.relax.owl.admin.service;

import com.relax.owl.admin.service.impl.*;
import com.relax.owl.admin.convert.*;
import com.relax.owl.admin.domain.dto.*;
import com.relax.owl.admin.domain.entity.*;
import com.relax.owl.admin.mapper.*;
import com.relax.owl.common.minio.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ContentStatusAuthorizationTest {
    @Test void videoEditorCannotChangeStatusThroughGeneralUpdate() {
        var mapper=mock(HeroVideoMapper.class);
        var convert=mock(HeroVideoConvert.class);
        var storage=mock(FileStorageService.class);
        var service=new HeroVideoServiceImpl(mapper,convert,storage);
        var existing=new HeroVideoDO(); existing.setId(1L); existing.setStatus(1);
        when(mapper.selectById(1L)).thenReturn(existing);
        var dto=new HeroVideoDTO(); dto.setId(1L); dto.setStatus(0);
        assertThrows(AccessDeniedException.class,()->service.update(dto));
        verifyNoInteractions(storage);
        dto.setStatus(1);
        var update=new HeroVideoDO(); update.setStatus(1);
        when(convert.DTOConvertDO(dto)).thenReturn(update);
        assertTrue(service.update(dto));
        verify(mapper).updateById(argThat((HeroVideoDO row)->row.getStatus()==null));
    }
    @Test void menuEditorCannotChangeStatusThroughGeneralUpdate() {
        var mapper=mock(MenuItemMapper.class);
        var convert=mock(MenuItemConvert.class);
        var storage=mock(FileStorageService.class);
        var service=new MenuItemServiceImpl(mapper,convert,storage);
        var existing=new MenuItemDO(); existing.setId(1L); existing.setStatus(1);
        when(mapper.selectById(1L)).thenReturn(existing);
        var dto=new MenuItemDTO(); dto.setId(1L); dto.setStatus(0);
        assertThrows(AccessDeniedException.class,()->service.update(dto));
        verifyNoInteractions(storage);
        dto.setStatus(1);
        var update=new MenuItemDO(); update.setStatus(1);
        when(convert.DTOConvertDO(dto)).thenReturn(update);
        assertTrue(service.update(dto));
        verify(mapper).updateById(argThat((MenuItemDO row)->row.getStatus()==null));
    }
}
