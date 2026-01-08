package com.rapido.captainapp.data.repository

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.rapido.captainapp.data.local.OrderDao
import com.rapido.captainapp.data.local.toDomain
import com.rapido.captainapp.data.local.toEntity
import com.rapido.captainapp.domain.model.Order
import com.rapido.captainapp.domain.model.OrderStatus
import com.rapido.captainapp.domain.usecase.OrderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


class OrderRepositoryImpl(
    private val orderDao: OrderDao
) : OrderRepository {

    // For dummy pending order (simulating Firebase notifications)
    private val _pendingOrder = MutableStateFlow<Order?>(null)
    private val firebaseStore = FirebaseFirestore.getInstance()
    private val orderCollection = firebaseStore.collection("orders")

    init {
       //writeDummyOnDB()
       listenToOrderFirestoreDatabase()
    }

    init {
      val respositoryScope = CoroutineScope(Dispatchers.Main)
        respositoryScope.launch {
            _pendingOrder?.collect {  order ->
                Log.d("OrderDebug", "Pending order value changed: ${order?.id ?: "null"}")
            }
        }
    }
    override fun getActiveOrders(): Flow<List<Order>> {
        return orderDao.getActiveOrders().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    private fun writeDummyOnDB() {
        val dummyOrder = createDummyOrder()
        orderCollection.document(dummyOrder.id)
            .set(dummyOrder)
    }

    private fun deleteActiveOrderFromFireStore(
        order: Order
    ) {
        orderCollection.document(order.id)
            .delete()
            .addOnSuccessListener {
                // Log or handle the success (e.g., show a Toast)
                Log.d(TAG, "DocumentSnapshot successfully deleted!")
            }
            .addOnFailureListener { e ->
                // Log or handle the error
                Log.w(TAG, "Error deleting document", e)
            }
    }

    private fun listenToOrderFirestoreDatabase() {
        orderCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("OrderDebug", "Error listening to orders: $error")
                return@addSnapshotListener
            }
            if (snapshot != null) {
                // compelete this
                val orders = snapshot.documents.mapNotNull { doc ->
                    try {
                        // Assuming your Order has a companion object or function to parse from Firestore
                        val id = doc.getString("id") ?: doc.id
                        val pickupAddress = doc.getString("pickupAddress") ?: ""
                        val deliveryAddress = doc.getString("deliveryAddress") ?: ""
                        val customerName = doc.getString("customerName") ?: ""
                        val amount = doc.getDouble("amount") ?: 0.0
                        val distance = doc.getString("distance") ?: ""
                        val status = doc.getString("status")?.let { OrderStatus.valueOf(it) } ?: OrderStatus.ASSIGNED

                        Order(
                            id = id,
                            pickupAddress = pickupAddress,
                            deliveryAddress = deliveryAddress,
                            customerName = customerName,
                            amount = amount,
                            distance = distance,
                            status = status
                        )

                    } catch (e: Exception) {
                        Log.e("OrderDebug", "Error parsing order: ${e.message}")
                        null
                    }
                }
                _pendingOrder.value = orders.firstOrNull()
            }
        }
    }

    suspend fun writeToDB(orders: List<Order>)  {
            orders.forEach { order ->
                val orderEntity = order.toEntity()
                orderDao.insertOrder(orderEntity)
            }
    }

    override suspend fun getOrderById(orderId: String): Order? {
        return orderDao.getOrderById(orderId)?.toDomain()
    }

    override suspend fun acceptOrder(orderId: String): Result<Order> {
        return try {
            // Simulate API delay
            delay(500)

            val order = _pendingOrder.value?.copy(status = OrderStatus.ASSIGNED)
                ?: throw Exception("No pending order")

            // Save to local database
            orderDao.insertOrder(order.toEntity())

            // delete from remote
            deleteActiveOrderFromFireStore(order)
            // Clear local val
            _pendingOrder.value = null

            Result.success(order)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun rejectOrder(orderId: String): Result<Unit> {
        return try {
            delay(300)
            _pendingOrder.value = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Result<Order> {
        return try {
            delay(500)

            val orderEntity = orderDao.getOrderById(orderId)
                ?: throw Exception("Order not found")

            val updatedEntity = orderEntity.copy(status = status.name)
            orderDao.updateOrder(updatedEntity)

            // If delivered, remove from active orders after a delay
            if (status == OrderStatus.DELIVERED) {
                delay(1000)
               // orderDao.deleteOrder(orderId)
            }

            Result.success(updatedEntity.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getPendingOrder(): Flow<Order?> {
        return _pendingOrder
    }

    // DUMMY: Simulate incoming order (call this to test)
//    override suspend fun simulateIncomingOrder() {
//        delay(3000) // Wait 3 seconds after going on duty
//
//        val dummyOrder = createDummyOrder()
//        _pendingOrder.value = dummyOrder
//    }

//    override suspend  fun getPastOrders(): Flow<List<Order>> {
//        return orderDao.getPastOrders()
//                    .map { entities ->
//            entities.map { it.toDomain() }
//        }
//    }

    override suspend  fun getPastOrders(): Flow<List<Order>> {
        return orderDao.getPastOrders()
                    .map { entities ->
            entities.map { it.toDomain() }
        }
    }
    private fun createDummyOrder(): Order {
        val orderNumber = (1000..9999).random()
        return Order(
            id = "ORD$orderNumber",
            pickupAddress = "McDonald's, Koramangala",
            deliveryAddress = "BTM Layout, 2nd Stage",
            customerName = "Rahul Sharma",
            amount = 250.0,
            distance = "3.5 km",
            status = OrderStatus.ASSIGNED
        )
    }
}