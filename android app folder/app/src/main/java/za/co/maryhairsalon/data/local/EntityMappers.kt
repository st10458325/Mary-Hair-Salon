package za.co.maryhairsalon.data.local

import za.co.maryhairsalon.data.local.entity.BookingEntity
import za.co.maryhairsalon.data.local.entity.CategoryEntity
import za.co.maryhairsalon.data.local.entity.ServiceEntity
import za.co.maryhairsalon.data.local.entity.UserEntity
import za.co.maryhairsalon.data.model.Booking
import za.co.maryhairsalon.data.model.BookingStatus
import za.co.maryhairsalon.data.model.BookingType
import za.co.maryhairsalon.data.model.Category
import za.co.maryhairsalon.data.model.Role
import za.co.maryhairsalon.data.model.Service
import za.co.maryhairsalon.data.model.User

/** Room entity <-> domain model conversions. */

fun UserEntity.toDomain() = User(
    id = id, name = name, surname = surname, email = email, contact = contact,
    role = Role.from(role), title = title, photoUrl = photoUrl,
    notificationsEnabled = notificationsEnabled,
)

fun User.toEntity() = UserEntity(
    id = id, name = name, surname = surname, email = email, contact = contact,
    role = role.dbValue, title = title, photoUrl = photoUrl,
    notificationsEnabled = notificationsEnabled,
)

fun CategoryEntity.toDomain() = Category(id = id, label = label, description = description, image = image, icon = icon)

fun Category.toEntity() = CategoryEntity(id = id, label = label, description = description, image = image, icon = icon)

fun ServiceEntity.toDomain() = Service(
    id = id, title = title, categoryId = categoryId, price = price,
    description = description, tags = tags, image = image, discount = discount, isActive = isActive,
)

fun Service.toEntity() = ServiceEntity(
    id = id, title = title, categoryId = categoryId, price = price,
    description = description, tags = tags, image = image, discount = discount, isActive = isActive,
)

fun BookingEntity.toDomain() = Booking(
    id = id, customerId = customerId, stylistId = stylistId, stylistName = stylistName,
    serviceId = serviceId, serviceTitle = serviceTitle, categoryId = categoryId,
    categoryLabel = categoryLabel, name = name, surname = surname, contact = contact,
    type = BookingType.from(type), location = location, date = date, time = time,
    notes = notes, cost = cost, discount = discount, total = total,
    status = BookingStatus.from(status), createdAt = createdAt, updatedAt = updatedAt,
)

fun Booking.toEntity() = BookingEntity(
    id = id, customerId = customerId, stylistId = stylistId, stylistName = stylistName,
    serviceId = serviceId, serviceTitle = serviceTitle, categoryId = categoryId,
    categoryLabel = categoryLabel, name = name, surname = surname, contact = contact,
    type = type.dbValue, location = location, date = date, time = time,
    notes = notes, cost = cost, discount = discount, total = total,
    status = status.dbValue, createdAt = createdAt, updatedAt = updatedAt,
)
